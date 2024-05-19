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

var m = sync.Map{}
var aliveChan = make(chan *TunnelBo)
var reChan = make(chan *TunnelBo)

func main() {
	go handleKeepAlive()
	i := 0
	for {
		i += 1
		log.Printf("Try to tunnel => %v", i)
		/* 构建隧道 */
		tunnelBo, err := buildTunnelBo()
		if err != nil {
			time.Sleep(2 * time.Second)
			continue
		}
		aliveChan <- tunnelBo
		/* 处理隧道 */
		go handleTunnel(tunnelBo)
		/* 重建隧道 */
		<-reChan
		time.Sleep(5 * time.Second)
	}
}

func buildTunnelBo() (*TunnelBo, error) {
	/* 创建配置 */
	homePath, _ := os.UserHomeDir()
	privateKeyPath := filepath.Join(homePath, ".ssh", "id_rsa")
	privateKeyBytes, err := os.ReadFile(privateKeyPath)
	if err != nil {
		log.Fatalf("[buildTunnelBo] Failed to read => %v", privateKeyPath)
	}
	privateKey, err := ssh.ParsePrivateKey(privateKeyBytes)
	if err != nil {
		log.Fatalf("[buildTunnelBo] Failed to parse private key => %v", err)
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
	remoteClient, err := ssh.Dial("tcp", "124.71.35.157:22", clientConfig)
	if err != nil {
		log.Printf("[buildTunnelBo] Failed to dial => %v", err)
		return nil, err
	}
	remoteListener, err := remoteClient.Listen("tcp", "localhost:10006")
	if err != nil {
		_ = remoteClient.Close()
		log.Printf("[buildTunnelBo] Failed to create reverse tunnel => %v", err)
		return nil, err
	}
	log.Printf("[buildTunnelBo] Listening on remote side...")
	tunnelBo := TunnelBo{
		remoteClient:   remoteClient,
		remoteListener: &remoteListener,
		id:             uuid.New().String(),
	}
	return &tunnelBo, nil
}

func handleTunnel(tunnelBo *TunnelBo) {
	remoteListener := tunnelBo.remoteListener
	remoteClient := tunnelBo.remoteClient
	defer func() {
		_ = (*remoteListener).Close()
	}()
	defer func() {
		_ = remoteClient.Close()
	}()
	for {
		status, _ := m.Load(tunnelBo.id)
		log.Printf("[handleTunnel] %v.status = %v", tunnelBo.id, status)
		if status != nil && status == "0" {
			return
		}
		userConn, err := (*remoteListener).Accept()
		if err != nil {
			log.Printf("[handleTunnel] Failed to accept connection => %v", err)
			return
		}
		go handleTunnelDo(userConn)
	}
}

func handleTunnelDo(userConn net.Conn) {
	localConn, err := net.Dial("tcp", "localhost:10006")
	if err != nil {
		log.Printf("[handleTunnelDo] Failed to connect to local service => %v", err)
		return
	}
	defer func() {
		_ = userConn.Close()
	}()
	defer func() {
		_ = localConn.Close()
	}()

	wg := sync.WaitGroup{}
	wg.Add(2)
	go func() {
		_, err := io.Copy(localConn, userConn)
		if err != nil {
			log.Printf("[handleTunnelDo] Failed to copy => %v", err)
		}
		wg.Done()
	}()
	go func() {
		_, err := io.Copy(userConn, localConn)
		if err != nil {
			log.Printf("[handleTunnelDo] Failed to copy => %v", err)
		}
		wg.Done()
	}()
	wg.Wait()
}

func handleKeepAlive() {
	for {
		aliveEle := <-aliveChan
		go func() {
			log.Printf("[handleKeepAlive] Detecting => %v", aliveEle.id)
			session, err := aliveEle.remoteClient.NewSession()
			if err != nil {
				log.Printf("[handleKeepAlive] Failed to newSession => %v", err)
				reChan <- aliveEle
				m.Store(aliveEle.id, "0")
				return
			}
			_, err = session.CombinedOutput("echo 1")
			if err != nil {
				log.Printf("[handleKeepAlive] Failed to combinedOutput => %v", err)
				reChan <- aliveEle
				m.Store(aliveEle.id, "0")
				return
			}
			aliveChan <- aliveEle
		}()
		time.Sleep(5 * time.Second)
	}
}

type TunnelBo struct {
	remoteClient   *ssh.Client
	remoteListener *net.Listener
	id             string
}
