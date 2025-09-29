package dto

type LoginRequest struct {
	Username string `json:"username" binding:"required"`
	Password string `json:"password" binding:"required"`
}

type CreateUserRequest struct {
	Username string `json:"username" binding:"required,min=2,max=50"`
	Email    string `json:"email" binding:"required,email"`
	Password string `json:"password" binding:"required,min=6"`
}

type UpdateUserRequest struct {
	Username string `json:"username" binding:"omitempty,min=3,max=50"`
	Email    string `json:"email" binding:"omitempty,email"`
}
type GetUserListRequest struct {
	PageRequest
	Username string `form:"username"`
	Email    string `form:"email"`
	Page     int    `form:"page,default=1"`
}

type GetUserByIDRequest struct {
	ID uint `json:"id" binding:"required"`
}
