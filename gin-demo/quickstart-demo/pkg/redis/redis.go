package redis

import (
	"context"
	"quickstart-demo/pkg/config"
	"quickstart-demo/pkg/logger"
	"time"

	"github.com/redis/go-redis/v9"
	"go.uber.org/zap"
)

var (
	Client *redis.Client
	Ctx    = context.Background()
)

func InitRedis(cfg config.RedisConfig) bool {
	Client = redis.NewClient(&redis.Options{
		Addr:            cfg.Addr,
		DialTimeout:     5 * time.Second,
		ReadTimeout:     3 * time.Second,
		WriteTimeout:    3 * time.Second,
		PoolSize:        100,
		MinIdleConns:    10,
		PoolTimeout:     4 * time.Second,
		ConnMaxIdleTime: 5 * time.Minute,
		MaxRetries:      3,
		MinRetryBackoff: 8 * time.Millisecond,
		MaxRetryBackoff: 512 * time.Millisecond,
	})

	if err := Client.Ping(Ctx).Err(); err != nil {
		logger.Error("测试 Redis 连接错误", zap.String("Addr", cfg.Addr), zap.Error(err))
		return false
	}

	return true
}
