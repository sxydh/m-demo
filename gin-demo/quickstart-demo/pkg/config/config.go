package config

import (
	"errors"
	"os"
	"path/filepath"
	"quickstart-demo/configs"
	"quickstart-demo/pkg/logger"
	"strconv"

	"go.uber.org/zap"
	"gopkg.in/yaml.v3"
)

type Config struct {
	ServerConfig   ServerConfig   `yaml:"server"`
	DatabaseConfig DatabaseConfig `yaml:"database"`
	RedisConfig    RedisConfig    `yaml:"redis"`
	AuthConfig     AuthConfig     `yaml:"auth"`
}

type ServerConfig struct {
	Port int `yaml:"port"`
}

type DatabaseConfig struct {
	Host     string `yaml:"host"`
	Port     int    `yaml:"port"`
	Username string `yaml:"username"`
	Password string `yaml:"password"`
	Database string `yaml:"database"`
	LogMode  bool   `yaml:"logMode"`
}

type RedisConfig struct {
	Addr string `yaml:"addr"`
}

type AuthConfig struct {
	Secret string `yaml:"secret"`
}

var (
	ServerCfg   ServerConfig
	DatabaseCfg DatabaseConfig
	RedisCfg    RedisConfig
	AuthCfg     AuthConfig
)

func Init() {
	config := &Config{}

	// 内嵌的配置文件
	loadFromEmbedded(config)
	// 运行目录下的配置文件
	loadFromFile(config)
	// 环境变量
	loadFromEnv(config)

	AuthCfg = config.AuthConfig
	ServerCfg = config.ServerConfig
	DatabaseCfg = config.DatabaseConfig
	RedisCfg = config.RedisConfig
}

func loadFromEmbedded(config *Config) {
	data, err := configs.EmbeddedConfigs.ReadFile(getConfigFileName())
	if err != nil && !errors.Is(err, os.ErrNotExist) {
		logger.Debug("读取内嵌配置文件错误", zap.Error(err))
		return
	}

	if err := yaml.Unmarshal(data, config); err != nil {
		logger.Debug("解析内嵌配置文件错误", zap.Error(err))
	}
}

func loadFromFile(config *Config) {
	exePath, err := os.Executable()
	if err != nil {
		logger.Debug("获取运行目录错误", zap.Error(err))
		return
	}

	exeDir := filepath.Dir(exePath)
	configFile := filepath.Join(exeDir, getConfigFileName())
	data, err := os.ReadFile(configFile)
	if err != nil && !errors.Is(err, os.ErrNotExist) {
		logger.Debug("读取运行目录配置文件错误", zap.String("file", configFile), zap.Error(err))
		return
	}

	if err := yaml.Unmarshal(data, config); err != nil {
		logger.Debug("解析运行目录配置文件错误", zap.Error(err))
		return
	}
}

func loadFromEnv(config *Config) {
	if port := os.Getenv("SERVER.PORT"); port != "" {
		config.ServerConfig.Port, _ = strconv.Atoi(port)
	}
}

func getConfigFileName() string {
	env := os.Getenv("ENV")
	if env == "production" || env == "prod" {
		return "config.prod.yaml"
	}
	return "config.dev.yaml"
}
