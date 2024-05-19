package main

import (
	"golang.org/x/crypto/ssh"
	"io"
	"log"
	"net"
	"os"
	"path/filepath"
	"sync"
	"time"
)

/*
 * 前置条件：
 * 1、与远程主机可以免密登录。
 *
 * 编译命令：
 * set GOOS=windows
 * set GOARCH=amd64
 * go build -o rtunnel_demo.exe
 */
func main() {
	i := 0
	for {
		i += 1
		log.Printf("%v", i)
		SshTunnel()
	}
}

func SshTunnel() {
	/* 创建远程主机的连接配置 */
	homePath, _ := os.UserHomeDir()
	privateKeyPath := filepath.Join(homePath, ".ssh", "id_rsa")
	privateKeyBytes, err := os.ReadFile(privateKeyPath)
	if err != nil {
		log.Fatalf("Failed to read: %v", privateKeyPath)
	}
	privateKey, err := ssh.ParsePrivateKey(privateKeyBytes)
	if err != nil {
		log.Fatalf("Failed to parse private key: %v", err)
	}

	sshConfig := &ssh.ClientConfig{
		User: "root",
		Auth: []ssh.AuthMethod{
			ssh.PublicKeys(privateKey),
		},
		HostKeyCallback: ssh.InsecureIgnoreHostKey(),
	}

	/* 创建远程主机的连接 */
	sshConn, err := ssh.Dial("tcp", "124.71.35.157:22", sshConfig)
	if err != nil {
		log.Printf("Failed to dial: %v", err)
		time.Sleep(5 * time.Second)
		return
	}
	defer func(closeable *ssh.Client) {
		err := closeable.Close()
		if err != nil {
			log.Printf("Failed to close: %v", err)
		}
	}(sshConn)

	/* 监听远程主机端口 */
	remoteListen, err := sshConn.Listen("tcp", "localhost:10006")
	if err != nil {
		log.Printf("Failed to create reverse tunnel: %v", err)
		time.Sleep(5 * time.Second)
		return
	}
	log.Printf("Listening on remote side...")
	defer func(closeable net.Listener) {
		err := closeable.Close()
		if err != nil {
			log.Printf("Failed to close: %v", err)
		}
	}(remoteListen)

	/* 处理远程主机进出流量 */
	for {
		userConn, err := remoteListen.Accept()
		if err != nil {
			log.Printf("Failed to accept connection: %v", err)
			return
		}
		go handleTunnel(userConn)
	}
}

func handleTunnel(userConn net.Conn) {
	localConn, err := net.Dial("tcp", "localhost:10006")
	if err != nil {
		log.Printf("Failed to connect to local service: %v", err)
		return
	}
	defer func(closeable net.Conn) {
		err = closeable.Close()
		if err != nil {
			log.Printf("Failed to close: %v", err)
		}
	}(userConn)
	defer func(closeable net.Conn) {
		err = closeable.Close()
		if err != nil {
			log.Printf("Failed to close: %v", err)
		}
	}(localConn)

	wg := sync.WaitGroup{}
	wg.Add(2)
	go func() {
		_, err := io.Copy(localConn, userConn)
		if err != nil {
			log.Printf("Failed to copy: %v", err)
		}
		wg.Done()
	}()
	go func() {
		_, err := io.Copy(userConn, localConn)
		if err != nil {
			log.Printf("Failed to copy: %v", err)
		}
		wg.Done()
	}()
	wg.Wait()
}
