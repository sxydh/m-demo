package main

import (
	"bufio"
	"fmt"
	"net"
	"os"
)

func main() {
	/* 建立连接 */
	conn, err := net.Dial("tcp", "localhost:8080")
	if err != nil {
		fmt.Println("Error connecting: ", err)
		return
	}
	defer func(conn net.Conn) {
		err := conn.Close()
		if err != nil {
			fmt.Println("Error closing: ", err)
		}
	}(conn)

	scanner := bufio.NewScanner(os.Stdin)
	for {
		fmt.Print("Enter message: ")
		scanner.Scan()
		input := scanner.Text()

		/* 发送消息 */
		_, err := conn.Write([]byte(input))
		if err != nil {
			fmt.Println("Error writing: ", err)
			return
		}

		/* 接收消息 */
		bytes := make([]byte, 1024)
		_, err = conn.Read(bytes)
		if err != nil {
			fmt.Println("Error reading: ", err)
			return
		}
		fmt.Println("Message reading: ", string(bytes))
	}
}
