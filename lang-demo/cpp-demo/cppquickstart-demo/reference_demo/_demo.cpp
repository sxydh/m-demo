// ReSharper disable All
#include <iostream>

/************ 引用定义 ************/
void reference_demo()
{
	// 引用相当于变量的别名，内部实现是指针常量。
	// 引用不可以更改，始终和初始值绑定在一起。
	int var_int = 1;
	std::string var_string = "Hello World!";
	int& ref_int = var_int;
	std::string& ref_string = var_string;
	std::cout << "引用 ref_int 为：" << ref_int << "，地址为：" << &ref_int << "，原始变量 var_int 地址为：" << &var_int << std::endl;
	std::cout << "引用 ref_string 为：" << ref_string << "，地址为：" << &ref_string << "，原始变量 var_string 地址为：" << &var_string << std::endl;
}

/************ 常量引用 ************/
void refrence_const_demo()
{
	// 常量引用可以用字面量初始化，非常量引用不可以。
	const int& const_ref_int = 1;
	const std::string& const_ref_string = "Hello World!";
	std::cout << "引用 const_ref_int 为：" << const_ref_int << "，地址为：" << &const_ref_int << std::endl;
	std::cout << "引用 const_ref_string 为：" << const_ref_string << "，地址为：" << &const_ref_string << std::endl;
}

int main(int argc, char* argv[])
{
	std::cout << std::endl << "//////////// reference_demo" << std::endl;
	reference_demo();

	std::cout << std::endl << "//////////// refrence_const_demo" << std::endl;
	refrence_const_demo();
}
