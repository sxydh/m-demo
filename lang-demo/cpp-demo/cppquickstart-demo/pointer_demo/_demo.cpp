// ReSharper disable All
#include <iostream>

/************ 指针定义 ************/
// pointer是一种特殊的数据类型
// pointer保存的是所指向数据的内存地址
void pointer_demo()
{
	// 如果定义时未初始化（即野指针），此时它可能指向内存中的任意区域。
	int* ptr_int;
	std::string* ptr_string;
}

/************ 指针大小 ************/
void pointer_size_demo()
{
	// 指针占用空间与系统有关，64位系统占用64位，32位系统占用32位。
	int* ptr_int;
	std::string* ptr_string;
	std::cout << "指针 ptr_int 占用为：" << sizeof(ptr_int) << std::endl;
	std::cout << "指针 ptr_string 占用为：" << sizeof(ptr_string) << std::endl;
}

/************ 指针初始化 ************/
void pointer_init_demo()
{
	int var_int = 1;
	std::string var_string = "Hello World!";
	int* ptr_int = &var_int;
	std::string* ptr_string = &var_string;
	std::cout << "指针 ptr_int 为：" << ptr_int << "，指向内容为：" << *ptr_int << std::endl;
	std::cout << "指针 ptr_string 为：" << ptr_string << "，指向内容为：" << *ptr_string << std::endl;
}

/************ 指针解引用 ************/
void pointer_dereference_demo()
{
	int var_int = 1;
	std::string var_string = "Hello World!";
	int* ptr_int = &var_int;
	std::string* ptr_string = &var_string;
	std::cout << "指针 ptr_int 指向内容为：" << *ptr_int << std::endl;
	std::cout << "指针 ptr_string 指向内容为：" << *ptr_string << std::endl;
}

/************ 空指针 ************/
void pointer_null_demo()
{
	// 如果pointer定义时还不知道具体指向，可以初始化为空指针（即0地址）。
	int* ptr_int_null = nullptr;
	int* ptr_int_null2 = NULL;
	int* ptr_int_null3 = 0;
	std::cout << "空指针 ptr_int_null 为：" << ptr_int_null << std::endl;
	std::cout << "空指针 ptr_int_null2 为：" << ptr_int_null2 << std::endl;
	std::cout << "空指针 ptr_int_null3 为：" << ptr_int_null3 << std::endl;
}

/************ 无类型指针 ************/
void pointer_void_demo()
{
	// 如果指针定义时还不知道具体指向的类型，可以用void修饰，即无类型指针。
	// 无类型指针一般用来比较地址，或作为函数的输入输出。
	// 无类型指针不支持修改所指向的数据，即不能解引用。
	int var_int = 1;
	std::string var_string = "Hello World!";
	void* ptr_void;
	ptr_void = &var_int;
	std::cout << "指针 ptr_void 此时指向类型为：" << typeid(ptr_void).name() << std::endl;
	ptr_void = &var_string;
	std::cout << "指针 ptr_void 此时指向类型为：" << typeid(ptr_void).name() << std::endl;
}

/************ 指向指针的指针 ************/
void pointer_pointer_demo()
{
	int var_int = 1;
	std::string var_string = "Hello World!";
	int* ptr_int = &var_int;
	std::string* ptr_string = &var_string;
	int** ptr_ptr_int = &ptr_int;
	std::string** ptr_ptr_string = &ptr_string;
	std::cout << "指针 ptr_ptr_int 为：" << ptr_ptr_int << "，内容为：" << *ptr_ptr_int << std::endl;
	std::cout << "指针 ptr_ptr_string 为：" << ptr_ptr_string << "，内容为：" << *ptr_ptr_string << std::endl;
}

/************ 指针和常量 ************/
void pointer_const_demo()
{
	// 常量指针
	// 指针所指向的数据是常量，不能通过指针修改所指向的数据。
	int var_int = 1;
	std::string var_string = "Hello World!";
	const int* const_ptr_int = &var_int;
	const std::string* const_ptr_string = &var_string;
	std::cout << "指针 const_ptr_int 为：" << const_ptr_int << "，内容为：" << *const_ptr_int << std::endl;
	std::cout << "指针 const_ptr_string 为：" << const_ptr_string << "，内容为：" << *const_ptr_string << std::endl;

	// 指针常量
	// 指针保存的地址是常量，不能修改指针的指向。
	int* const ptr_const_int = &var_int;
	std::string* const ptr_const_string = &var_string;
	std::cout << "指针 ptr_const_int 为：" << ptr_const_int << "，内容为：" << *ptr_const_int << std::endl;
	std::cout << "指针 ptr_const_string 为：" << ptr_const_string << "，内容为：" << *ptr_const_string << std::endl;
}

int main(int argc, char* argv[])
{
	std::cout << std::endl << "//////////// pointer_demo" << std::endl;
	pointer_demo();

	std::cout << std::endl << "//////////// pointer_size_demo" << std::endl;
	pointer_size_demo();

	std::cout << std::endl << "//////////// pointer_init_demo" << std::endl;
	pointer_init_demo();

	std::cout << std::endl << "//////////// pointer_dereference_demo" << std::endl;
	pointer_dereference_demo();

	std::cout << std::endl << "//////////// pointer_null_demo" << std::endl;
	pointer_null_demo();

	std::cout << std::endl << "//////////// pointer_void_demo" << std::endl;
	pointer_void_demo();

	std::cout << std::endl << "//////////// pointer_pointer_demo" << std::endl;
	pointer_pointer_demo();

	std::cout << std::endl << "//////////// pointer_const_demo" << std::endl;
	pointer_const_demo();
}
