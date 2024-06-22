package collection_demo

import "fmt"

func ArrayDemo() {
	/* 创建数组 */
	intArray := [3]int{1, 2, 3}
	fmt.Println(intArray)

	/* 访问数组 */
	fmt.Println(intArray[0])
	fmt.Println(intArray[1])
	fmt.Println(intArray[2])

	/* 切片数组 */
	sliceArray := intArray[0:2]
	fmt.Println(sliceArray)
}
