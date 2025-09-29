package middleware

import (
	"quickstart-demo/pkg/logger"
	"time"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

func Logger() gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()

		c.Next()

		duration := time.Since(start)
		logger.Debug(c.Request.URL.Path,
			zap.String("方法", c.Request.Method),
			zap.Int("状态", c.Writer.Status()),
			zap.String("耗时", duration.String()))
	}
}
