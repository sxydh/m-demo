package middleware

import (
	"quickstart-demo/pkg/auth"
	"quickstart-demo/pkg/config"
	"quickstart-demo/pkg/e"
	"quickstart-demo/pkg/restful"
	"strings"

	"github.com/gin-gonic/gin"
)

func Auth() gin.HandlerFunc {
	return func(c *gin.Context) {
		excludePaths := []string{
			"/api/v1/user/register",
			"/api/v1/user/login",
		}

		currentPath := c.Request.URL.Path
		for _, path := range excludePaths {
			if strings.HasPrefix(currentPath, path) {
				c.Next()
				return
			}
		}

		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			restful.Fail(c, e.UnauthorizedError)
			c.Abort()
			return
		}

		tokenString := strings.TrimPrefix(authHeader, "Bearer ")
		if tokenString == authHeader {
			restful.Fail(c, e.UnauthorizedError)
			c.Abort()
			return
		}

		claims, err := auth.ValidateToken(config.AuthCfg.Secret, tokenString)
		if err != nil {
			restful.Fail(c, e.UnauthorizedError)
			c.Abort()
			return
		}

		c.Set("userID", claims.UserID)
		c.Set("username", claims.Username)
		c.Next()
	}
}
