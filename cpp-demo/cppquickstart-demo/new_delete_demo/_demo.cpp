// ReSharper disable All
#include <iostream>
#include <string>

class ClassDemo
{
public:
	int public_int = 1;
	std::string public_string = "Hello World!";

	ClassDemo()
	{
		std::cout << "构造函数 ClassDemo() 被调用" << std::endl;
	}

	~ClassDemo()
	{
		std::cout << "析构函数 ClassDemo 被调用" << std::endl;
	}
};

/************ 手动分配和回收内存 ************/
void new_delete_demo()
{
	// new 分配内存，并创建对象。
	// new 返回的是指针
	ClassDemo* ptr_class_demo = new ClassDemo;
	std::cout << "ptr_class_demo 为：" << ptr_class_demo->public_int << "，" << ptr_class_demo->public_string << std::endl;

	// delete 显示回收内存
	// new 创建的对象在堆区，方法结束后不会被回收，程序结束后才会被回收。
	// 如果 new 对象之后没有显示 delete，可能会造成OOM。
	delete ptr_class_demo;
}

int main(int argc, char* argv[])
{
	std::cout << std::endl << "//////////// new_delete_demo" << std::endl;
	new_delete_demo();
}
