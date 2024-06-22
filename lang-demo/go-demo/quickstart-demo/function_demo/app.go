package function_demo

import "fmt"

func FunctionDemo() {
	argDemo("Hello, World!")

	fmt.Println(returnDemo())

	var strVar, strVar2, strVar3, strVar4 = returnMultiDemo()
	fmt.Printf("%s%s%s%s\n", strVar, strVar2, strVar3, strVar4)
}

func argDemo(stringVar string) {
	fmt.Println(stringVar)
}

func returnDemo() string {
	return "Hello, World!"
}

func returnMultiDemo() (string, string, string, string) {
	return "Hello", ", ", "World", "!"
}
