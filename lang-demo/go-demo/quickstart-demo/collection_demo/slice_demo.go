package collection_demo

import (
	"fmt"
	"strconv"
)

func SliceDemo() {
	/* 切片 Slice 是动态数组 */
	/* 创建 */
	var slice []int

	/* 修改 */
	slice = append(slice, 1)
	slice = append(slice, 2)
	slice = append(slice, 3)

	/* 访问 */
	fmt.Println(strconv.Itoa(slice[0]))
	fmt.Println(strconv.Itoa(slice[1]))
	fmt.Println(strconv.Itoa(slice[2]))
}
