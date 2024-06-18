package main

import (
	"fmt"
	"net"
)

func main() {
	/* 监听端口 */
	listener, err := net.Listen("tcp", "localhost:8080")
	if err != nil {
		fmt.Println("Error listening: ", err)
		return
	}
	defer func(listener net.Listener) {
		err := listener.Close()
		if err != nil {
			fmt.Println("Error closing: ", err)
		}
	}(listener)
	fmt.Println("Server started, listening on port 8080")

	/* 接收连接 */
	for {
		conn, err := listener.Accept()
		if err != nil {
			fmt.Println("Error accepting connection: ", err)
			return
		}
		go handleConnection(conn)
	}
}

func handleConnection(conn net.Conn) {
	defer func(conn net.Conn) {
		err := conn.Close()
		if err != nil {
			fmt.Println("Error closing: ", err)
		}
	}(conn)

	for {
		/* 读入消息 */
		bytes := make([]byte, 1024)
		_, err := conn.Read(bytes)
		if err != nil {
			fmt.Println("Error reading: ", err)
			return
		}
		fmt.Println("Message reading: ", string(bytes))

		/* 写出消息 */
		_, err = conn.Write(bytes)
		if err != nil {
			fmt.Println("Error writing: ", err)
			return
		}
	}
}
