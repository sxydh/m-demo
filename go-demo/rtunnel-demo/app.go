package main

import (
	"github.com/google/uuid"
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

/* 哈希表存储隧道状态 */
var m = sync.Map{}

/* 存活的隧道 */
var doingChan = make(chan *TunnelBo)

/* 待处理的隧道 */
var todoChan = make(chan *TunnelBo)

func main() {
	/* 初始化待处理的隧道 */
	todoChan <- &TunnelBo{id: uuid.New().String(), localPort: 20010, remoteIp: "124.71.35.157", remotePort: 20010}
	todoChan <- &TunnelBo{id: uuid.New().String(), localPort: 20020, remoteIp: "124.71.35.157", remotePort: 20020}
	todoChan <- &TunnelBo{id: uuid.New().String(), localPort: 20030, remoteIp: "124.71.35.157", remotePort: 20030}
	todoChan <- &TunnelBo{id: uuid.New().String(), localPort: 20040, remoteIp: "124.71.35.157", remotePort: 20040}

	/* 隧道保活协程 */
	go handleKeepAlive()

	for {
		/* 构建待处理的隧道 */
		tunnelBo := <-todoChan
		err := initTunnelBo(tunnelBo)
		if err != nil {
			time.Sleep(2 * time.Second)
			continue
		}
		// 构建成功后放入存活列表
		doingChan <- tunnelBo
		// 隧道通信协程
		go handleTunnel(tunnelBo)
		time.Sleep(5 * time.Second)
	}
}

func initTunnelBo(tunnelBo *TunnelBo) error {
	/* 创建配置 */
	homePath, _ := os.UserHomeDir()
	privateKeyPath := filepath.Join(homePath, ".ssh", "id_rsa")
	privateKeyBytes, err := os.ReadFile(privateKeyPath)
	if err != nil {
		log.Fatalf("os.ReadFile error: %v", privateKeyPath)
	}
	privateKey, err := ssh.ParsePrivateKey(privateKeyBytes)
	if err != nil {
		log.Fatalf("ssh.ParsePrivateKey error: %v", err)
	}

	clientConfig := &ssh.ClientConfig{
		User: "root",
		Auth: []ssh.AuthMethod{
			ssh.PublicKeys(privateKey),
		},
		Timeout:         5 * time.Second,
		HostKeyCallback: ssh.InsecureIgnoreHostKey(),
	}

	/* 创建隧道 */
	remoteClient, err := ssh.Dial("tcp", tunnelBo.remoteIp+":22", clientConfig)
	if err != nil {
		log.Printf("ssh.Dial error: %v", err)
		return err
	}
	remoteListener, err := remoteClient.Listen("tcp", "localhost:"+string(tunnelBo.remotePort))
	if err != nil {
		_ = remoteClient.Close()
		log.Printf("remoteClient.Listen error: %v", err)
		return err
	}
	log.Printf("remoteClient.Listening...")

	tunnelBo.remoteClient = remoteClient
	tunnelBo.remoteListener = &remoteListener
	return nil
}

func handleTunnel(tunnelBo *TunnelBo) {
	remoteListener := tunnelBo.remoteListener
	remoteClient := tunnelBo.remoteClient
	//goland:noinspection GoUnhandledErrorResult
	defer (*remoteListener).Close()
	//goland:noinspection GoUnhandledErrorResult
	defer remoteClient.Close()
	for {
		status, _ := m.Load(tunnelBo.id)
		if status != nil && status == "0" {
			return
		}
		userConn, err := (*remoteListener).Accept()
		if err != nil {
			log.Printf("remoteListener.Accept error: %v", err)
			return
		}
		go handleTunnelDo(tunnelBo, &userConn)
	}
}

func handleTunnelDo(tunnelBo *TunnelBo, userConn *net.Conn) {
	localConn, err := net.Dial("tcp", "localhost:"+string(tunnelBo.localPort))
	if err != nil {
		log.Printf("net.Dial error: %v", err)
		return
	}
	//goland:noinspection GoUnhandledErrorResult
	defer (*userConn).Close()
	//goland:noinspection GoUnhandledErrorResult
	defer localConn.Close()

	wg := sync.WaitGroup{}
	wg.Add(2)

	go func() {
		_, err := io.Copy(localConn, *userConn)
		if err != nil {
			log.Printf("io.Copy error: %v", err)
		}
		wg.Done()
	}()
	go func() {
		_, err := io.Copy(*userConn, localConn)
		if err != nil {
			log.Printf("io.Copy error: %v", err)
		}
		wg.Done()
	}()

	wg.Wait()
}

func handleKeepAlive() {
	for {
		tunnelBo := <-doingChan
		go func() {
			session, err := tunnelBo.remoteClient.NewSession()
			if err != nil {
				log.Printf("remoteClient.NewSession error: %v", err)
				todoChan <- tunnelBo
				m.Store(tunnelBo.id, "0")
				return
			}
			_, err = session.CombinedOutput("echo 1")
			if err != nil {
				log.Printf("session.CombinedOutput error: %v", err)
				todoChan <- tunnelBo
				m.Store(tunnelBo.id, "0")
				return
			}
			doingChan <- tunnelBo
		}()
		time.Sleep(5 * time.Second)
	}
}

type TunnelBo struct {
	remoteClient   *ssh.Client
	remoteListener *net.Listener
	id             string
	localPort      int32
	remoteIp       string
	remotePort     int32
}
