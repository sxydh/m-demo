// ReSharper disable All
#include<iostream>


/************ 基本类型 ************/
void fundamental_type_demo()
{
	// bool
	// 最小8位
	// 本质是整型
	bool var_bool = true;

	// char
	// 8位
	char var_char = 'a';

	// short
	// 16位
	short var_short = 1;

	// int
	// 32位
	int var_int = 1;

	// long
	// 32位
	long var_long = 1;

	// float
	// 32位
	float var_float = 1.1f;

	// long long
	// 64位
	long long var_ll = 1;

	// double
	// 64位
	double var_double = 1.1;

	std::cout << "var_bool 类型为：" << typeid(var_bool).name() << "，占用：" << sizeof(var_bool) << std::endl;
	std::cout << "var_char 类型为：" << typeid(var_char).name() << "，占用：" << sizeof(var_char) << std::endl;
	std::cout << "var_short 类型为：" << typeid(var_short).name() << "，占用：" << sizeof(var_short) << std::endl;
	std::cout << "var_int 类型为：" << typeid(var_int).name() << "，占用：" << sizeof(var_int) << std::endl;
	std::cout << "var_long 类型为：" << typeid(var_long).name() << "，占用：" << sizeof(var_long) << std::endl;
	std::cout << "var_float 类型为：" << typeid(var_float).name() << "，占用：" << sizeof(var_float) << std::endl;
	std::cout << "var_ll 类型为：" << typeid(var_ll).name() << "，占用：" << sizeof(var_ll) << std::endl;
	std::cout << "var_double 类型为：" << typeid(var_double).name() << "，占用：" << sizeof(var_double) << std::endl;
}

/************ 无符号类型 ************/
void unsigned_type_demo()
{
	// 符号占用一位
	// 无符号类型表示范围比有符号类型大一倍
	unsigned int var_unsigned_int = 1;

	std::cout << "var_unsigned_int 类型为：" << typeid(var_unsigned_int).name() << "，值为：" << var_unsigned_int << std::endl;
}

/************ 数组类型 ************/
void array_type_demo()
{
	// 数组创建
	// 初始化时，若没有显示指定值，则默认为0。
	int arr_int[3] = {1, 2, 3};
	int arr_int2[3] = {1, 2};
	int arr_int3[] = {1, 2, 3};
	int arr_int4[3];

	// 数组大小
	std::cout << "数组 arr_int 占用：" << sizeof(arr_int) << std::endl;
	std::cout << "数组 arr_int[0] 占用：" << sizeof(arr_int[0]) << std::endl;
	std::cout << "数组 arr_int 长度：" << sizeof(arr_int) / sizeof(arr_int[0]) << std::endl;

	// 数组访问
	arr_int4[0] = 1, arr_int4[1] = 2;
	arr_int4[2] = 3;

	// 数组遍历
	std::cout << "遍历 arr_int2：";
	for (int i = 0; i < sizeof(arr_int2) / sizeof(arr_int2[0]); i++)
	{
		std::cout << "arr_int2[" << i << "]=" << arr_int2[i] << "，";
	}
	std::cout << std::endl;

	std::cout << "遍历 arr_int：";
	for (int e : arr_int)
	{
		std::cout << e << "，";
	}
	std::cout << std::endl;

	// 数组本质
	// 数组名是个指针，指向数组的第一个元素。
	std::cout << "数组 arr_int 的地址为：" << &arr_int << std::endl;
	int* ptr_arr_int = arr_int;
	std::cout << "指针 ptr_arr_int 为：" << ptr_arr_int << "，指向内容为：" << *ptr_arr_int << std::endl;
	std::cout << "利用指针 ptr_arr_int 遍历 arr_int：";
	for (int i = 0; i < sizeof(arr_int) / sizeof(arr_int[0]); i++)
	{
		std::cout << "arr_int[" << i << "]=" << *(ptr_arr_int + i) << "，";
	}
	std::cout << std::endl;
}

/************ 字符串类型 ************/
void string_demo()
{
	// 创建字符串
	std::string str = "Hello";
	std::string str2(" ");
	std::string str3(1, 'W');
	std::string str4 = str + str2 + str3 + "orld!";
	std::cout << "字符串 str4：" << str4 << std::endl;

	// 字符串内部实现是数组
	// 0 是结束符，是必需的。
	char arr_char[6] = {'H', 'e', 'l', 'l', 'o', '\0'};
	std::string str5 = arr_char;
	char arr_char2[8] = " World!";
	std::string str6 = str5 + arr_char2;
	std::cout << "字符串 str6：" << str6 << std::endl;
}

/************ 类型转换 ************/
void type_convert_demo()
{
	// 隐式类型转换
	int var_int = 1;
	long var_long = 1;
	std::cout << "var_int + var_long 类型为：" << typeid(var_int + var_long).name() << std::endl;

	// 强制类型转换
	std::cout << "(double)var_int 类型为：" << typeid((double)var_int).name() << std::endl;
}

int main()
{
	std::cout << std::endl << "//////////// fundamental_type_demo" << std::endl;
	fundamental_type_demo();

	std::cout << std::endl << "//////////// unsigned_type_demo" << std::endl;
	unsigned_type_demo();

	std::cout << std::endl << "//////////// array_type_demo" << std::endl;
	array_type_demo();

	std::cout << std::endl << "//////////// string_demo" << std::endl;
	string_demo();

	std::cout << std::endl << "//////////// type_convert_demo" << std::endl;
	type_convert_demo();
}
