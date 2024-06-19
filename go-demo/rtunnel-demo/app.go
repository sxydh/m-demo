package main

import (
	"fmt"
	"github.com/sxydh/mgo-util/ssh_utils"
	"io"
	"log"
	"os"
	"time"
)

/*
 * 基于 SSH 的反向隧道示例
 *
 * 前置条件：
 * 1、两边主机可以免密建立 SSH 连接
 * 2、本地私钥文件路径为： C:/Users/Administrator/.ssh/id_rsa 。
 *
 * 编译命令：
 * set GOOS=windows
 * set GOARCH=amd64
 * go build -o bin/rtunnel_demo.exe
 */

//goland:noinspection GoUnhandledErrorResult
func main() {
	/* 日志配置 */
	// 输出日期和时间
	log.SetFlags(log.Ldate | log.Ltime)
	// 输出到控制台和文件
	logPath := "./logs"
	_ = os.Mkdir(logPath, os.ModePerm)
	logPath += "/" + time.Now().Format("2006-01-02") + ".log"
	file, err := os.OpenFile(logPath, os.O_CREATE|os.O_APPEND|os.O_RDWR, os.ModePerm)
	if err != nil {
		fmt.Printf("OpenFile error: logPath=%v, err=%v\r\n", logPath, err)
	}
	defer file.Close()
	writer := io.MultiWriter(os.Stdout, file)
	log.SetOutput(writer)

	ssh_utils.NewReverseTunnel(&[]*ssh_utils.Tunnel{
		{SSHIp: "124.71.35.157", SSHPort: 22, SSHUser: "root", ListenPort: 40010, TargetIp: "localhost", TargetPort: 20010},
		{SSHIp: "124.71.35.157", SSHPort: 22, SSHUser: "root", ListenPort: 40020, TargetIp: "localhost", TargetPort: 20020},
		{SSHIp: "124.71.35.157", SSHPort: 22, SSHUser: "root", ListenPort: 40030, TargetIp: "localhost", TargetPort: 10006},
	})
}
