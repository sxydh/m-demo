package main

import (
	"github.com/sxydh/mgo-util/ssh_utils"
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
 * go build -o rtunnel_demo.exe
 */

func main() {
	ssh_utils.NewReverseTunnel(&[]*ssh_utils.Tunnel{
		{SSHIp: "124.71.35.157", SSHPort: 22, SSHUser: "root", ListenPort: 40010, TargetIp: "localhost", TargetPort: 40010},
		{SSHIp: "124.71.35.157", SSHPort: 22, SSHUser: "root", ListenPort: 40020, TargetIp: "localhost", TargetPort: 40020},
		{SSHIp: "124.71.35.157", SSHPort: 22, SSHUser: "root", ListenPort: 40030, TargetIp: "localhost", TargetPort: 10006},
	})
}
