package handler

import (
	"quickstart-demo/internal/dto"
	"quickstart-demo/internal/service"
	"quickstart-demo/pkg/e"
	"quickstart-demo/pkg/restful"

	"github.com/gin-gonic/gin"
)

type UserHandler struct {
	userService service.UserService
}

func NewUserHandler(userService service.UserService) *UserHandler {
	return &UserHandler{
		userService: userService,
	}
}

func (h *UserHandler) Register(c *gin.Context) {
	var req dto.CreateUserRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		restful.Fail(c, e.BadRequestError)
		return
	}

	user, err := h.userService.Register(&req)
	if err != nil {
		restful.Fail(c, err)
		return
	}

	restful.Success(c, user)
}

func (h *UserHandler) Login(c *gin.Context) {
	var req dto.LoginRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		restful.Fail(c, e.BadRequestError)
		return
	}

	response, err := h.userService.Login(&req)
	if err != nil {
		restful.Fail(c, err)
		return
	}

	restful.Success(c, response)
}

func (h *UserHandler) GetUserList(c *gin.Context) {
	var req dto.GetUserListRequest
	if err := c.ShouldBindQuery(&req); err != nil {
		restful.Fail(c, e.BadRequestError)
		return
	}

	page, err := h.userService.GetUserList(&req)
	if err != nil {
		restful.Fail(c, err)
		return
	}

	restful.Success(c, page)
}

func (h *UserHandler) GetUserByID(c *gin.Context) {
	var req dto.GetUserByIDRequest
	if err := c.ShouldBindUri(&req); err != nil {
		restful.Fail(c, e.BadRequestError)
		return
	}

	user, err := h.userService.GetUserByID(req.ID)
	if err != nil {
		restful.Fail(c, err)
		return
	}

	restful.Success(c, user)
}

func (h *UserHandler) UpdateUser(c *gin.Context) {
	var uriReq dto.GetUserByIDRequest
	if err := c.ShouldBindUri(&uriReq); err != nil {
		restful.Fail(c, e.BadRequestError)
		return
	}

	var bodyReq dto.UpdateUserRequest
	if err := c.ShouldBindJSON(&bodyReq); err != nil {
		restful.Fail(c, e.BadRequestError)
		return
	}

	user, err := h.userService.UpdateUser(uriReq.ID, &bodyReq)
	if err != nil {
		restful.Fail(c, err)
		return
	}

	restful.Success(c, user)
}

func (h *UserHandler) DeleteUser(c *gin.Context) {
	var req dto.GetUserByIDRequest
	if err := c.ShouldBindUri(&req); err != nil {
		restful.Fail(c, e.BadRequestError)
		return
	}

	err := h.userService.DeleteUser(req.ID)
	if err != nil {
		restful.Fail(c, err)
		return
	}

	restful.Success(c, nil)
}
