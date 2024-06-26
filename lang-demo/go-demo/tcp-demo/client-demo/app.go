package main

import (
	"bufio"
	"encoding/binary"
	"fmt"
	"io"
	"log"
	"net"
	"os"
)

//goland:noinspection GoUnhandledErrorResult
func main() {
	/* 建立连接 */
	address := "localhost:30010"
	conn, err := net.Dial("tcp", address)
	if err != nil {
		log.Printf("Dial error: address=%v, err=%v", address, err)
		return
	}
	defer conn.Close()

	/* 接收消息 */
	go func() {
		reader := bufio.NewReader(conn)
		for {
			bytes := make([]byte, 4)
			_, err = io.ReadFull(reader, bytes)
			if err != nil {
				log.Printf("Read body length error, localAddr=%v, remoteAddr=%v, err=%v", conn.LocalAddr(), conn.RemoteAddr(), err)
				return
			}
			bytes = make([]byte, binary.BigEndian.Uint32(bytes))
			_, err = io.ReadFull(reader, bytes)
			if err != nil {
				log.Printf("Read body error, localAddr=%v, remoteAddr=%v, err=%v", conn.LocalAddr(), conn.RemoteAddr(), err)
				return
			}
			log.Printf("Get message: msg=%v", string(bytes))
		}
	}()

	/* 发送消息 */
	fmt.Println("Enter message: ")
	scanner := bufio.NewScanner(os.Stdin)
	for {
		scanner.Scan()
		input := scanner.Text()

		// 解决粘包问题
		bytes := make([]byte, 4)
		binary.BigEndian.PutUint32(bytes, uint32(len(input)))
		_, err = conn.Write(bytes)
		if err != nil {
			log.Printf("Write body length error: err=%v", err)
			return
		}
		_, err = conn.Write([]byte(input))
		if err != nil {
			log.Printf("Write body error: err=%v", err)
			return
		}
	}
}
