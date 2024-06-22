package collection_demo

import "fmt"

func MapDemo() {
	/* 创建映射 */
	m := make(map[string]string)
	m["Hello"] = "World"

	/* 访问映射 */
	fmt.Printf("Hello = %s\n", m["Hello"])
}
