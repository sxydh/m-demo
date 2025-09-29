package service

import (
	"quickstart-demo/internal/dto"
	"quickstart-demo/internal/model"
	"quickstart-demo/internal/repository"
	"quickstart-demo/pkg/auth"
	"quickstart-demo/pkg/config"
	"quickstart-demo/pkg/e"

	"golang.org/x/crypto/bcrypt"
)

type UserService interface {
	Register(req *dto.CreateUserRequest) (*dto.UserResponse, *e.BizError)
	Login(req *dto.LoginRequest) (*dto.LoginResponse, *e.BizError)
	UpdateUser(id uint, req *dto.UpdateUserRequest) (*dto.UserResponse, *e.BizError)
	GetUserList(req *dto.GetUserListRequest) (*dto.PageResponse, *e.BizError)
	GetUserByID(id uint) (*dto.UserResponse, *e.BizError)
	DeleteUser(id uint) *e.BizError
}

type userService struct {
	userRepo repository.UserRepository
}

func NewUserService(userRepo repository.UserRepository) UserService {
	return &userService{userRepo: userRepo}
}

func (s *userService) Register(req *dto.CreateUserRequest) (*dto.UserResponse, *e.BizError) {
	existingUser, err := s.userRepo.FindByUsername(req.Username)
	if err != nil {
		return nil, e.InternalError
	}
	if existingUser != nil {
		return nil, e.UsernameExistError
	}

	existingUser, err = s.userRepo.FindByEmail(req.Email)
	if err != nil {
		return nil, e.InternalError
	}
	if existingUser != nil {
		return nil, e.EmailExistError
	}

	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(req.Password), bcrypt.DefaultCost)
	if err != nil {
		return nil, e.InternalError
	}

	user := &model.User{
		Username: req.Username,
		Email:    req.Email,
		Password: string(hashedPassword),
	}

	if err := s.userRepo.Create(user); err != nil {
		return nil, e.InternalError
	}

	return dto.ToUserResponse(user), nil
}

func (s *userService) Login(req *dto.LoginRequest) (*dto.LoginResponse, *e.BizError) {
	user, err := s.userRepo.FindByUsername(req.Username)
	if err != nil {
		return nil, e.InternalError
	}
	if user == nil {
		user, err = s.userRepo.FindByEmail(req.Username)
		if err != nil {
			return nil, e.InternalError
		}
		if user == nil {
			return nil, e.UserNotExistError
		}
	}

	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(req.Password)); err != nil {
		return nil, e.LoginError
	}

	token, err := auth.GenerateToken(config.AuthCfg.Secret, &auth.Claims{UserID: user.ID, Username: user.Username})
	if err != nil {
		return nil, e.InternalError
	}

	return &dto.LoginResponse{
		User:  *dto.ToUserResponse(user),
		Token: token,
	}, nil
}

func (s *userService) UpdateUser(id uint, req *dto.UpdateUserRequest) (*dto.UserResponse, *e.BizError) {
	user, err := s.userRepo.FindByID(id)
	if err != nil {
		return nil, e.InternalError
	}
	if user == nil {
		return nil, e.UserNotExistError
	}

	if req.Username != "" {
		existingUser, err := s.userRepo.FindByUsername(req.Username)
		if err != nil {
			return nil, e.InternalError
		}
		if existingUser != nil && existingUser.ID != id {
			return nil, e.UsernameExistError
		}
		user.Username = req.Username
	}

	if req.Email != "" {
		existingUser, err := s.userRepo.FindByEmail(req.Email)
		if err != nil {
			return nil, e.InternalError
		}
		if existingUser != nil && existingUser.ID != id {
			return nil, e.EmailExistError
		}
		user.Email = req.Email
	}

	if err := s.userRepo.Update(user); err != nil {
		return nil, e.InternalError
	}

	return dto.ToUserResponse(user), nil
}

func (s *userService) GetUserList(req *dto.GetUserListRequest) (*dto.PageResponse, *e.BizError) {
	list, total, err := s.userRepo.FindList(req)
	if err != nil {
		return nil, e.InternalError
	}
	return &dto.PageResponse{
		Total: total,
		List:  list,
	}, nil
}

func (s *userService) GetUserByID(id uint) (*dto.UserResponse, *e.BizError) {
	user, err := s.userRepo.FindByID(id)
	if err != nil {
		return nil, e.InternalError
	}
	if user == nil {
		return nil, e.UserNotExistError
	}

	return dto.ToUserResponse(user), nil
}

func (s *userService) DeleteUser(id uint) *e.BizError {
	user, err := s.userRepo.FindByID(id)
	if err != nil {
		return e.InternalError
	}
	if user == nil {
		return e.UserNotExistError
	}

	if err := s.userRepo.Delete(id); err != nil {
		return e.InternalError
	}

	return nil
}
