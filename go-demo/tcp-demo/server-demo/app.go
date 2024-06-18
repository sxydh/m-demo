package main

import (
	"bufio"
	"encoding/binary"
	"io"
	"log"
	"net"
	"strconv"
)

//goland:noinspection GoUnhandledErrorResult
func main() {
	/* 监听端口 */
	port := 30010
	listener, err := net.Listen("tcp", ":"+strconv.Itoa(port))
	if err != nil {
		log.Printf("Listen on port for tcp error: port=%v, err=%v", port, err)
		return
	}
	defer listener.Close()
	log.Printf("Listening for tcp: port=%v", port)

	/* 接收连接 */
	for {
		conn, err := listener.Accept()
		if err != nil {
			log.Printf("Accept connection error: port=%v, err=%v", port, err)
			return
		}
		log.Printf("Accepting connection: localAddr=%v， remoteAddr=%v", conn.LocalAddr(), conn.RemoteAddr())

		/* 接收消息 */
		go func() {
			defer conn.Close()
			reader := bufio.NewReader(conn)
			for {
				bytes := make([]byte, 4)
				_, err = io.ReadFull(reader, bytes)
				if err != nil {
					log.Printf("Read body length error, localAddr=%v， remoteAddr=%v", conn.LocalAddr(), conn.RemoteAddr())
					return
				}
				bytes = make([]byte, binary.BigEndian.Uint32(bytes))
				_, err = io.ReadFull(reader, bytes)
				if err != nil {
					log.Printf("Read body error, localAddr=%v， remoteAddr=%v", conn.LocalAddr(), conn.RemoteAddr())
					return
				}
				msg := string(bytes)
				log.Printf("Get message: msg=%v", msg)

				/* 回复消息 */
				// 解决粘包问题
				bytes = make([]byte, 4)
				binary.BigEndian.PutUint32(bytes, uint32(len(msg)))
				_, err = conn.Write(bytes)
				if err != nil {
					log.Printf("Write body length error: err=%v", err)
					return
				}
				bytes = []byte(msg)
				_, err = conn.Write(bytes)
				if err != nil {
					log.Printf("Write body error: err=%v", err)
					return
				}
			}
		}()
	}
}
