package main

import (
	"log"
	"net/http"
)

/* 创建一个静态资源网站 */
func main() {
	rootDir := "./ROOT"
	addr := ":8080"
	fileHandler := http.FileServer(http.Dir(rootDir))
	http.Handle("/", fileHandler)
	log.Printf("ListenAndServe: addr=%v", addr)
	err := http.ListenAndServe(addr, nil)
	if err != nil {
		log.Fatalf("ListenAndServe error: err=%v", err)
	}
}
