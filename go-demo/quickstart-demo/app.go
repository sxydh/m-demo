/*
 * 程序的入口是 main 包
 */
package main

import (
	"fmt"
	"quickstart-demo/collection_demo"
	"quickstart-demo/function_demo"
	"quickstart-demo/goroutine_demo"
	"quickstart-demo/package_demo"
	"quickstart-demo/pointer_demo"
	"quickstart-demo/struct_demo"
	"quickstart-demo/type_demo"
	"quickstart-demo/var_demo"
)

func main() {
	fmt.Println(">>>>>>>>>>> VarDemo")
	var_demo.VarDemo()
	fmt.Println()

	fmt.Println(">>>>>>>>>>> TypeDemo")
	type_demo.TypeDemo()
	fmt.Println()

	fmt.Println(">>>>>>>>>>> PointerDemo")
	pointer_demo.PointerDemo()
	fmt.Println()

	fmt.Println(">>>>>>>>>>> ArrayDemo")
	collection_demo.ArrayDemo()
	fmt.Println()

	fmt.Println(">>>>>>>>>>> MapDemo")
	collection_demo.MapDemo()
	fmt.Println()

	fmt.Println(">>>>>>>>>>> PackageDemo")
	package_demo.PackageDemo()
	fmt.Println()

	fmt.Println(">>>>>>>>>>> FunctionDemo")
	function_demo.FunctionDemo()
	fmt.Println()

	fmt.Println(">>>>>>>>>>> StructDemo")
	struct_demo.StructDemo()
	fmt.Println()

	fmt.Println(">>>>>>>>>>> GoroutineDemo")
	goroutine_demo.GoroutineDemo()
	fmt.Println()
}
