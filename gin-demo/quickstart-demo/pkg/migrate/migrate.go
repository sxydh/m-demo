package migrate

import (
	"database/sql"
	"errors"
	"quickstart-demo/pkg/config"
	"quickstart-demo/pkg/logger"

	_ "github.com/golang-migrate/migrate/v4/source/file"

	"github.com/golang-migrate/migrate/v4"
	"github.com/golang-migrate/migrate/v4/database/mysql"
	"go.uber.org/zap"
)

func InitMigrate(cfg config.DatabaseConfig, db *sql.DB) bool {
	driver, err := mysql.WithInstance(db, &mysql.Config{})
	if err != nil {
		logger.Error("创建数据库驱动错误", zap.Error(err))
		return false
	}

	m, err := migrate.NewWithDatabaseInstance(
		"file://migrations",
		cfg.Database,
		driver,
	)
	if err != nil {
		logger.Error("创建迁移实例错误", zap.Error(err))
		return false
	}

	if err := m.Up(); err != nil && !errors.Is(err, migrate.ErrNoChange) {
		logger.Error("执行迁移错误", zap.Error(err))
		return false
	}

	version, dirty, err := m.Version()
	if err != nil && !errors.Is(err, migrate.ErrNilVersion) {
		logger.Error("检查版本错误", zap.Error(err))
		return false
	}

	logger.Info("迁移数据库成功",
		zap.Uint("version", version),
		zap.Bool("dirty", dirty))
	return true
}
