package main

import (
	"fmt"
	"io"
	"log"
	"os"
)

//goland:noinspection GoUnhandledErrorResult
func main() {
	/* 日志配置 */
	// 输出日期和时间
	log.SetFlags(log.Ldate | log.Ltime)
	// 输出到控制台和文件
	logPath := "./logs"
	_ = os.Mkdir(logPath, os.ModePerm)
	logPath += "/app.log"
	file, err := os.OpenFile(logPath, os.O_CREATE|os.O_APPEND|os.O_RDWR, os.ModePerm)
	if err != nil {
		fmt.Printf("OpenFile error: logPath=%v, err=%v\r\n", logPath, err)
	}
	defer file.Close()
	writer := io.MultiWriter(os.Stdout, file)
	log.SetOutput(writer)

	log.Printf("Hello World!")
}
