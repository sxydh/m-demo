package struct_demo

import "fmt"

type vertex struct {
	X int
	Y int
}

func StructDemo() {
	v := vertex{1, 1}
	fmt.Printf("x = %d\n", v.X)
	fmt.Printf("y = %d\n", v.Y)
}
