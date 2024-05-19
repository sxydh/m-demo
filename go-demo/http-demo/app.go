package main

import (
	"fmt"
	"io"
	"log"
	"net/http"
)

func main() {
	http.HandleFunc("/", rootHandler)
	log.Fatal(http.ListenAndServe("localhost:8080", nil))
}

func rootHandler(w http.ResponseWriter, req *http.Request) {
	_, err := io.WriteString(w, "Hello World!")
	if err != nil {
		fmt.Println("Error writing: ", nil)
	}
}
