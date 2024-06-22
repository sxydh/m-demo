// ReSharper disable All
#include <iostream>

/************ 模板 ************/
// 模板是类型占位符
// 利用模板，同样的算法可以对不同类型的数据进行处理。

/************ 模板函数 ************/
// 模板语法关键字 typename 与 class 作用相似
// 模板可以有默认类型
template <typename T, class X = int>
void template_function_demo(T t, X x)
{
	std::cout << "函数 template_function_demo 被调用：t = " << t << "，x = " << x << std::endl;
}

/************ 模板类 ************/
// 模板语法关键字 typename 与 class 作用相似
// 模板可以有默认类型
template <typename T, class X = int>
class ClassDemo
{
public:
	T var_t;
	X var_x;
};

int main(int argc, char* argv[])
{
	[]
	{
		std::cout << std::endl << "//////////// 使用模板函数" << std::endl;
		template_function_demo(1, "Hello World!");
		template_function_demo("Hello World!", 1);
	}();

	[]
	{
		std::cout << std::endl << "//////////// 使用模板类" << std::endl;
		ClassDemo<int, std::string> var_class_demo = {1, "Hello World!"};
		std::cout << "对象 ClassDemo<int, std::string> var_class_demo 为：" << var_class_demo.var_t << "，" << var_class_demo.var_x << std::endl;

		ClassDemo<std::string, int> var_class_demo_2 = { "Hello World!", 1 };
		std::cout << "对象 ClassDemo<std::string, int> var_class_demo_2 为：" << var_class_demo_2.var_t << "，" << var_class_demo_2.var_x << std::endl;
	}();
}
