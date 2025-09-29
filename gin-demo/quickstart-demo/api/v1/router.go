package v1

import (
	"quickstart-demo/api/middleware"
	"quickstart-demo/internal/handler"

	"github.com/gin-gonic/gin"
)

type Router struct {
	userHandler *handler.UserHandler
}

func NewRouter(
	userHandler *handler.UserHandler,
) *Router {
	return &Router{
		userHandler: userHandler,
	}
}

func (r *Router) Register(engine *gin.Engine) {
	engine.Use(middleware.Logger())
	engine.Use(middleware.Auth())

	v1 := engine.Group("/api/v1")
	{
		userGroup := v1.Group("/user")
		{
			userGroup.POST("/register", r.userHandler.Register)
			userGroup.POST("/login", r.userHandler.Login)
			userGroup.GET("", r.userHandler.GetUserList)
			userGroup.GET("/:ID", r.userHandler.GetUserByID)
			userGroup.PUT("/:ID", r.userHandler.UpdateUser)
			userGroup.DELETE("/:ID", r.userHandler.DeleteUser)
		}
	}
}
