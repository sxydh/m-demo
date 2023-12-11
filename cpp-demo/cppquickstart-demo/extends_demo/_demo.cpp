// ReSharper disable All
#include <iostream>
#include <string>
#include <valarray>

class ClassDemo
{
public:
	int public_int = 1;

	ClassDemo()
	{
		std::cout << "构造函数 ClassDemo() 被调用" << std::endl;
	}
};

class ClassDemo2
{
public:
	std::string public_string = "ClassDemo2 的 Hello World!";

	ClassDemo2()
	{
		std::cout << "构造函数 ClassDemo2() 被调用" << std::endl;
	}
};

class ClassDemo3
{
public:
	std::string public_string = "ClassDemo3 的 Hello World!";

	ClassDemo3()
	{
		std::cout << "构造函数 ClassDemo3() 被调用" << std::endl;
	}
};

/************ 继承定义 ************/
// 可以继承基类的成员，但是不包括：静态成员，构造函数（包括拷贝和析构），重载运算符，友元定义。
//
// 继承修饰符：public，protected，private。默认private。
// public：继承自基本的成员的可见性不变。
// protected：继承自基类的成员的可见性最高为protected。
// private：继承自基类的成员的可见性最高为private。
//
// 派生类实例化时，会先实例化基类。

/************ 单继承 ************/
class SubClassDemo : public ClassDemo
{
public:
	SubClassDemo()
	{
		std::cout << "构造函数 SubClassDemo() 被调用" << std::endl;
	}
};

/************ 多继承 ************/
class SubClassDemo2 : public ClassDemo, public ClassDemo2
{
public:
	std::string public_string = "SubClassDemo2 的 Hello World!";

	SubClassDemo2()
	{
		std::cout << "构造函数 SubClassDemo2() 被调用" << std::endl;
		// 访问成员
		std::cout << "继承自基类的成员 SubClassDemo2::public_int = " << this->public_int << std::endl;
		// 访问基类成员
		std::cout << "基类成员 ClassDemo::public_int = " << ClassDemo::public_int << std::endl;
		// 如果派生类与基类成员同名，会优先取派生类。
		// 静态成员同名同理
		std::cout << "派生类与基类成员同名 SubClassDemo2::public_string = " << this->public_string << std::endl;
	}
};

class SubClassDemo3 : public ClassDemo, public ClassDemo2, public ClassDemo3
{
public:
	SubClassDemo3()
	{
		std::cout << "构造函数 SubClassDemo3() 被调用" << std::endl;
		// 如果基类之间成员同名，访问时必须加作用域解析运算符。
		std::cout << "基类成员 ClassDemo2::public_string = " << ClassDemo2::public_string << "，ClassDemo3::public_string = " << ClassDemo3::public_string << std::endl;
	}
};

class SubClassDemo4 : public ClassDemo
{
public:
	SubClassDemo4()
	{
		std::cout << "构造函数 SubClassDemo4() 被调用" << std::endl;
	}
};

class SubClassDemo5 : public ClassDemo
{
public:
	SubClassDemo5()
	{
		std::cout << "构造函数 SubClassDemo5() 被调用" << std::endl;
	}
};

class SubSubClassDemo : public SubClassDemo4, public SubClassDemo5
{
public:
	SubSubClassDemo()
	{
		std::cout << "构造函数 SubSubClassDemo() 被调用" << std::endl;
		// 对于菱形继承，会产生多个同名成员问题，访问时必须加作用域解析运算符。
		// 菱形继承问题还可以用虚继承解决（关键字 virtual，在第一层继承时声明，告诉编译器只保留同名成员的一份副本）
		std::cout << "基类成员 SubClassDemo4::public_int = " << SubClassDemo4::public_int << "，SubClassDemo5::public_int = " << SubClassDemo5::public_int << std::endl;
	}
};

void instance_order_demo()
{
	SubClassDemo var_sub_class_demo;
}

void base_sub_same_member_demo()
{
	SubClassDemo2 var_sub_class_demo2;
}

void base_base_same_member_demo()
{
	SubClassDemo3 var_sub_class_demo3;
}

void diamond_demo()
{
	SubSubClassDemo var_sub_sub_class_demo;
}

int main(int argc, char* argv[])
{
	std::cout << std::endl << "//////////// instance_order_demo" << std::endl;
	instance_order_demo();

	std::cout << std::endl << "//////////// base_sub_same_member_demo" << std::endl;
	base_sub_same_member_demo();

	std::cout << std::endl << "//////////// base_base_same_member_demo" << std::endl;
	base_base_same_member_demo();

	std::cout << std::endl << "//////////// diamond_demo" << std::endl;
	diamond_demo();
}
