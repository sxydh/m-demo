package main

import (
	"quickstart-demo/api/v1"
	"quickstart-demo/internal/handler"
	"quickstart-demo/internal/repository"
	"quickstart-demo/internal/service"
	"quickstart-demo/pkg/config"
	"quickstart-demo/pkg/database"
	"quickstart-demo/pkg/logger"
	"quickstart-demo/pkg/migrate"
	"quickstart-demo/pkg/redis"
	"strconv"

	"github.com/gin-gonic/gin"
)

func main() {
	if err := logger.InitLogger(); err != nil {
		panic(err)
	}

	// 加载配置
	logger.Debug("开始加载配置")
	config.Init()

	// 初始化组件
	if !database.InitMySQL(config.DatabaseCfg) {
		return
	}
	sqlDB, _ := database.DB.DB()
	if !migrate.InitMigrate(config.DatabaseCfg, sqlDB) {
		return
	}
	if !redis.InitRedis(config.RedisCfg) {
		return
	}

	// 注入依赖
	userRepo := repository.NewUserRepository(database.DB)
	userService := service.NewUserService(userRepo)
	userHandler := handler.NewUserHandler(userService)

	// 注册路由
	engine := gin.Default()
	router := v1.NewRouter(userHandler)
	router.Register(engine)

	// 启动服务
	if err := engine.Run(":" + strconv.Itoa(config.ServerCfg.Port)); err != nil {
		panic(err)
	}
}
