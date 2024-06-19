package main

import (
	"C"
)

func main() {
}

//export Add
func Add(a int, b int) int {
	return a + b
}
