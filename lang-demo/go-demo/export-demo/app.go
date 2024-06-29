package main

import (
	"C"
	"log"
	"math/rand"
	"net"
	"net/http"
	"strconv"
	"time"
)

func main() {
}

//export Fs
func Fs() int {
	r := rand.New(rand.NewSource(time.Now().UnixNano()))
	for {
		port := 40000 + r.Intn(10000)
		addr := ":" + strconv.Itoa(port)
		listener, err := net.Listen("tcp", addr)
		if err != nil {
			continue
		}
		_ = listener.Close()
		go func() {
			http.Handle("/", http.FileServer(http.Dir("./ROOT")))
			log.Printf("ListenAndServe going: addr=%v", addr)
			err = http.ListenAndServe(addr, nil)
			if err != nil {
				log.Printf("ListenAndServe error: err=%v", err)
			}
		}()
		return port
	}
}
