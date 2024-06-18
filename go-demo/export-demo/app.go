package main

import "time"

func main() {
}

//export FunctionName
func NowString() string {
	return time.Now().String()
}
