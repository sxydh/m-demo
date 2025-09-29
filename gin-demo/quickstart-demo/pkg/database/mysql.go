package database

import (
	"fmt"
	"quickstart-demo/pkg/config"
	"time"

	"quickstart-demo/pkg/logger"

	"go.uber.org/zap"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	gormlogger "gorm.io/gorm/logger"
	"gorm.io/gorm/schema"
)

var DB *gorm.DB

func InitMySQL(cfg config.DatabaseConfig) bool {
	dsn := fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?charset=utf8mb4&parseTime=True&loc=Local",
		cfg.Username,
		cfg.Password,
		cfg.Host,
		cfg.Port,
		cfg.Database,
	)

	gormConfig := &gorm.Config{
		NamingStrategy: schema.NamingStrategy{
			SingularTable: true,
		},
	}

	if cfg.LogMode {
		gormConfig.Logger = gormlogger.Default.LogMode(gormlogger.Info)
	} else {
		gormConfig.Logger = gormlogger.Default.LogMode(gormlogger.Silent)
	}

	rawDb, err := gorm.Open(mysql.Open(dsn), gormConfig)
	if err != nil {
		logger.Error("连接数据库错误", zap.String("dsn", dsn), zap.Error(err))
		return false
	}

	sqlDB, err := rawDb.DB()
	if err != nil {
		logger.Error("获取数据库对象错误", zap.Error(err))
		return false
	}

	sqlDB.SetMaxIdleConns(10)
	sqlDB.SetMaxOpenConns(100)
	sqlDB.SetConnMaxLifetime(1 * time.Hour)

	if err := sqlDB.Ping(); err != nil {
		logger.Error("测试数据库连接错误", zap.Error(err))
		return false
	}

	DB = rawDb
	logger.Debug("初始化数据库连接成功")
	return true
}
