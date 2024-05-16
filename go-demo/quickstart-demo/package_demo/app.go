package package_demo

import "fmt"

func PackageDemo() {
	fmt.Println("包名是路径的最后一个文件夹名")
	fmt.Println("程序从 main 包开始运行")
	fmt.Println("如果包内的元素以大写字母开头，则表示该元素是导出的，即外部可见的。")
}
