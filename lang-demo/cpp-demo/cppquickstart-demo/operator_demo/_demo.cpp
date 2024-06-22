// ReSharper disable All
#include<iostream>


/************ 基本运算符 ************/
void operator_demo()
{
	// 算术运算符
	int sum = 1 + 1;
	// 关系运算符
	bool is_equal = 1 == 2;
	// 逻辑运算符
	bool and_result = 1 && 0;
	// 位运算符
	int left_shift = 1 << 2;
	// 赋值运算符
	sum += 1;
	// 条件运算符
	int if_result = 1 ? 1 : 0;
}

/************ 作用域解析运算符 ************/
void operator_scope_resolution_demo()
{
	// 运算符标识为“::”，主要用于以下方面：
	// 1、类成员函数定义。不同的类可能有相同的成员函数名称，使用作用域运算符进行区分。
	// 2、显示使用全局变量。在函数内部局部变量会屏蔽全局变量，使用作用域运算符可以显示使用全局变量。
}

int main()
{
	std::cout << std::endl << "//////////// operator_demo" << std::endl;
	operator_demo();

	std::cout << std::endl << "//////////// operator_scope_resolution_demo" << std::endl;
	operator_scope_resolution_demo();
}
