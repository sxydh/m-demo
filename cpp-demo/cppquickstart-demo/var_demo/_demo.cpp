// ReSharper disable All
#include<iostream>


/************ 全局变量 ************/
int var_int = 1;

/************ 全局常量 ************/
constexpr int const_int = 1;

/************ 局部变量 ************/
void scope_var_demo()
{
	int var_int = 2;
	std::cout << "全局变量 var_int 为：" << ::var_int << std::endl;
	std::cout << "局部变量优先级比全局变量高，局部变量 var_int 为：" << var_int << std::endl;
}

int main()
{
	std::cout << std::endl << "//////////// scope_var_demo" << std::endl;
	scope_var_demo();
}
