// ReSharper disable All
#include <iostream>
#include <string>

/************ 函数声明 ************/
// 函数声明可以多次
// 函数声明仅仅只是为了告诉编译器函数名称及如何调用函数
// 函数声明和函数定义可以不在同一个文件内。例如：cpp_a文件定义了函数func_a，如果cpp_b文件想调用func_a的话，需要在cpp_b内声明func_a，然后才能调用。如果要声明的函数很多，可以将函数声明集中写在头文件内，然后再include头文件。
void function_demo();

/************ 函数定义 ************/
// 函数定义只能一次
void function_demo()
{
}

/************ 函数重载 ************/
// 函数名称相同，参数列表不同。
void function_overload_demo()
{
}

void function_overload_demo(int var_int)
{
}

void function_overload_demo(int var_int, std::string var_string = "Hello World!")
{
}

void function_overload_demo(int* ptr_int)
{
}

void function_overload_demo(int& ref_int)
{
}

/************ 函数返回值 ************/
std::string return_value_demo()
{
	std::string var_string = "Hello RETURN_VALUE_DEMO!";
	std::cout << "返回前地址：" << &var_string << std::endl;
	return var_string;
}

/************ 函数返回指针 ************/
std::string* return_pointer_demo()
{
	// 返回局部变量指针时，对指针的操作不能超过作用域。例如不能在函数外部对指针解引用。除非设置为static变量。
	static std::string static_var_string = "Hello RETURN_POINTER_DEMO!";
	std::cout << "返回前内容：" << static_var_string << std::endl;
	return &static_var_string;
}

/************ 函数返回引用 ************/
std::string& return_reference_demo()
{
	// 返回局部变量引用时，对引用的操作不能超过作用域。
	std::string var_string = "Hello RETURN_REFERENCE_DEMO!";
	std::cout << "返回前地址：" << &var_string << std::endl;
	std::string& ref_string = var_string;
	return ref_string;
}

void arg_none_demo()
{
	std::cout << "没有参数" << std::endl;
}

/************ 函数参数传递值 ************/
void arg_pass_by_value_demo(int var_int, std::string var_string)
{
	// 函数内部对变量的修改，不会影响函数外部变量的值。
	// 值传递实际是对实参的拷贝
	var_int = var_int * 100;
	var_string = var_string + " Hello ARG_PASS_BY_VALUE_DEMO!";
	std::cout << "已修改 var_int 为：" << var_int << "，var_string 为：" << var_string << std::endl;
}

/************ 函数参数传递指针 ************/
void arg_pass_by_pointer_demo(int* ptr_int, std::string* ptr_string)
{
	// 函数内部对变量的修改，会影响函数外部变量的值。
	*ptr_int = *ptr_int * 100;
	*ptr_string = *ptr_string + " Hello ARG_PASS_BY_POINTER_DEMO!";
	std::cout << "已修改数据 ptr_int 为：" << *ptr_int << "，ptr_string 为：" << *ptr_string << std::endl;
}

/************ 函数参数传递引用 ************/
void arg_pass_by_reference_demo(int& ref_int, std::string& ref_string)
{
	// 函数内部对变量的修改，会影响函数外部变量的值。
	ref_int = ref_int * 100;
	ref_string = ref_string + " Hello ARG_PASS_BY_REFERENCE_DEMO!";
	std::cout << "已修改 ref_int 为：" << ref_int << "，ref_string 为：" << ref_string << std::endl;
}

/************ 函数参数默认值 ************/
// 注意：对于函数func(a, b, c, d)，如果b设置了默认参数，则c，d都必须设置默认参数。
void arg_default_demo(int var_int, std::string var_string = "Hello World!", double var_double = 1.1)
{
	std::cout << "参数 var_int 为：" << var_int << "，var_string 为：" << var_string << "，var_double 为：" << var_double << std::endl;
}

/************ 内联函数定义 ************/
// 关键字 inline 修饰
// 在编译期间，编译器会把内联函数的代码副本放在所有调用内联函数的地方，这样可以避免函数调用的开销，但是会增加代码量。
// 如果某个函数的地址被采用或者由于过大（函数体过大）而无法内联，则编译器不会内联该函数。
inline void function_inline_demo()
{
}

/************ 函数重载运算符 ************/
struct StructDemo
{
	int var_int;
	std::string var_string;
};

// 加号重载
// 形参至少有一个是类类型
StructDemo operator+(StructDemo arg_struct_demo, StructDemo arg_struct_demo_2)
{
	std::cout << "函数 operator+(StructDemo, StructDemo) 被调用" << std::endl;
	StructDemo var_struct_demo = {arg_struct_demo.var_int + arg_struct_demo_2.var_int, arg_struct_demo.var_string + arg_struct_demo_2.var_string};
	return var_struct_demo;
}

int main(int argc, char* argv[])
{
	std::cout << std::endl << "//////////// function_demo" << std::endl;
	function_demo();

	std::cout << std::endl << "//////////// function_overload_demo" << std::endl;
	function_overload_demo();

	[]
	{
		std::cout << std::endl << "//////////// return_value_demo" << std::endl;
		std::string var_string = return_value_demo();
		std::cout << "返回后地址：" << &var_string << std::endl;
	}();

	[]
	{
		std::cout << std::endl << "//////////// return_pointer_demo" << std::endl;
		std::string* ptr_string = return_pointer_demo();
		std::cout << "返回后内容：" << *ptr_string << std::endl;
	}();

	[]
	{
		std::cout << std::endl << "//////////// return_reference_demo" << std::endl;
		std::string& ref_string = return_reference_demo();
		std::cout << "返回后地址：" << &ref_string << std::endl;
	}();

	[]
	{
		std::cout << std::endl << "//////////// arg_none_demo" << std::endl;
		arg_none_demo();
	}();

	[]
	{
		std::cout << std::endl << "//////////// arg_pass_by_value_demo" << std::endl;
		int var_int = 1;
		std::string var_string = "Hello ARG_PASS_BY_VALUE_DEMO!";
		std::cout << "修改前 var_int 为：" << var_int << "，var_string 为：" << var_string << std::endl;
		arg_pass_by_value_demo(var_int, var_string);
		std::cout << "修改后 var_int 为：" << var_int << "，var_string 为：" << var_string << std::endl;
	}();

	[]
	{
		std::cout << std::endl << "//////////// arg_pass_by_pointer_demo" << std::endl;
		int var_int = 1;
		std::string var_string = "Hello ARG_PASS_BY_POINTER_DEMO!";
		int* ptr_int = &var_int;
		std::string* ptr_string = &var_string;
		std::cout << "修改前数据 ptr_int 为：" << *ptr_int << "，ptr_string 为：" << *ptr_string << std::endl;
		arg_pass_by_pointer_demo(ptr_int, ptr_string);
		std::cout << "修改后数据 ptr_int 为：" << *ptr_int << "，ptr_string 为：" << *ptr_string << std::endl;
	}();

	[]
	{
		std::cout << std::endl << "//////////// arg_pass_by_reference_demo" << std::endl;
		int var_int = 1;
		std::string var_string = "Hello ARG_PASS_BY_REFERENCE_DEMO!";
		int& ref_int = var_int;
		std::string& ref_string = var_string;
		std::cout << "修改前 ref_int 为：" << ref_int << "，ref_string 为：" << ref_string << std::endl;
		arg_pass_by_reference_demo(ref_int, ref_string);
		std::cout << "修改后 ref_int 为：" << ref_int << "，ref_string 为：" << ref_string << std::endl;
	}();

	[]
	{
		std::cout << std::endl << "//////////// arg_pass_by_reference_demo" << std::endl;
		arg_default_demo(1);
	}();

	std::cout << std::endl << "//////////// function_inline_demo" << std::endl;
	function_inline_demo();

	[]
	{
		std::cout << std::endl << "//////////// operator+" << std::endl;
		StructDemo var_struct_demo = {1, "Hello World!"};
		StructDemo var_struct_demo_2 = var_struct_demo;
		StructDemo var_struct_demo_3 = operator+(var_struct_demo, var_struct_demo_2);
		std::cout << "加号重载输出 var_struct_demo_3 为：" << var_struct_demo_3.var_int << "，" << var_struct_demo_3.var_string << std::endl;
	}();
}
