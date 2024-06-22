package pointer_demo

import "fmt"

func PointerDemo() {
	/* 指针变量 */
	var pointerVar *string

	/* 指针获取 */
	stringVar := "Hello, World!"
	pointerVar = &stringVar
	fmt.Println(pointerVar)
}
