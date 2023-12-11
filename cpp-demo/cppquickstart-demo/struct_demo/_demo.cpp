// ReSharper disable All
#include <iostream>


/************ 结构体定义 ************/
// 结构体与类大部分相似，但是结构体成员可见性默认是 public 的，而类是 private 的。
struct StructDemo
{
	/************ 成员变量 ************/
	int var_int;
	std::string var_string;

	/************ 构造函数（可选） ************/
	StructDemo()
	{
		this->var_int = 1;
		this->var_string = "Hello World!";
	}

	StructDemo(int arg_int, std::string arg_string)
	{
		this->var_int = arg_int;
		this->var_string = arg_string;
	}
} struct_demo, struct_demo_2; // 声明变量（可选）

/************ 结构体初始化 ************/
void struct_init_demo()
{
	// 按顺序初始化
	StructDemo var_struct_demo = {1, "Hello World!"};
	std::cout << "结构体 var_struct_demo 为：" << var_struct_demo.var_int << "，" << var_struct_demo.var_string << std::endl;

	// 指定初始化
	StructDemo var_struct_demo_2;
	var_struct_demo_2.var_int = 1;
	var_struct_demo_2.var_string = "Hello World!";
	std::cout << "结构体 var_struct_demo_2 为：" << var_struct_demo_2.var_int << "，" << var_struct_demo_2.var_string << std::endl;

	// 构造器初始化
	StructDemo var_struct_demo_3(1, "Hello World!");
	std::cout << "结构体 var_struct_demo_3 为：" << var_struct_demo_3.var_int << "，" << var_struct_demo_3.var_string << std::endl;
}

/************ 结构体指针 ************/
void struct_as_pointer_demo()
{
	StructDemo struct_demo = {1, "Hello World!"};
	StructDemo* ptr_struct_demo = &struct_demo;
	std::cout << "指针 ptr_struct_demo 为：" << ptr_struct_demo << std::endl;
	std::cout << "指针 ptr_struct_demo 指向内容为：" << ptr_struct_demo->var_int << "，" << ptr_struct_demo->var_string << std::endl;
}

/************ 结构体数组 ************/
void struct_as_array_demo()
{
	StructDemo arr_struct_demo[3] = {
		{1, "Hello World!"},
		{2, "Hello World!Hello World!"},
		{3, "Hello World!Hello World!Hello World!"}
	};
	std::cout << "开始遍历：" << std::endl;
	for (StructDemo e : arr_struct_demo)
	{
		std::cout << e.var_int << "，" << e.var_string << std::endl;
	}
}

/************ 结构体作为参数 ************/
void struct_as_arg_demo(StructDemo var_struct_demo, StructDemo* ptr_struct_demo, StructDemo& ref_struct_demo)
{
	std::cout << "值传递 var_struct_demo 为：" << var_struct_demo.var_int << "，" << var_struct_demo.var_string << std::endl;
	std::cout << "指针传递 ptr_struct_demo 为：" << ptr_struct_demo->var_int << "，" << ptr_struct_demo->var_string << std::endl;
	std::cout << "引用传递 ref_struct_demo 为：" << ref_struct_demo.var_int << "，" << ref_struct_demo.var_string << std::endl;
}

/************ 结构体作为返回值 ************/
StructDemo struct_as_return_value_demo()
{
	StructDemo var_struct_demo = {1, "Hello World!"};
	std::cout << "返回前地址：" << &var_struct_demo << "，内容为：" << var_struct_demo.var_int << "，" << var_struct_demo.var_string << std::endl;
	return var_struct_demo;
}

int main(int argc, char* argv[])
{
	std::cout << std::endl << "//////////// struct_init_demo" << std::endl;
	struct_init_demo();

	std::cout << std::endl << "//////////// struct_as_pointer_demo" << std::endl;
	struct_as_pointer_demo();

	std::cout << std::endl << "//////////// struct_as_array_demo" << std::endl;
	struct_as_array_demo();

	[]
	{
		std::cout << std::endl << "//////////// struct_as_arg_demo" << std::endl;
		StructDemo var_struct_demo = {1, "Hello World!"};
		StructDemo& ref_struct_demo = var_struct_demo;
		struct_as_arg_demo(var_struct_demo, &var_struct_demo, ref_struct_demo);
	}();

	[]
	{
		std::cout << std::endl << "//////////// struct_as_return_value_demo" << std::endl;
		StructDemo var_struct_demo = struct_as_return_value_demo();
		var_struct_demo.var_int *= 100;
		var_struct_demo.var_string += var_struct_demo.var_string;
		std::cout << "返回后地址：" << &var_struct_demo << "，内容为：" << var_struct_demo.var_int << "，" << var_struct_demo.var_string << std::endl;
	}();
}
