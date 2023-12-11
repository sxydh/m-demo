// ReSharper disable All
#include <iostream>

/************ 多态 ************/
// 利用多态，可以实现一种类型不同的表现形式。
//
// 实现多态需要显示声明虚函数
// 多态使用时，如果子类有属性开辟到堆区，那么基类指针在释放时无法调用子类的析构代码。解决方式：将基类的析构函数改为虚析构函数或者纯虚析构函数。


class ClassDemo
{
public:
	void public_function();
	// 虚函数
	void virtual public_virtual_function();

	virtual ~ClassDemo()
	{
		std::cout << "类 ClassDemo 析构函数被调用" << std::endl;
	}
};

class ClassDemo2
{
public:
	// 纯虚函数
	// 如果虚函数实现需要延迟到派生类，可以声明为纯虚函数。
	// 包含纯虚函数的类是抽象类，无法实例化。
	void virtual public_pure_virtual_function() = 0;

	virtual ~ClassDemo2()
	{
		std::cout << "类 ClassDemo2 析构函数被调用" << std::endl;
	}
};

class SubClassDemo : public ClassDemo
{
public:
	void public_function();
	void public_virtual_function() override;

	virtual ~SubClassDemo()
	{
		std::cout << "类 SubClassDemo 析构函数被调用" << std::endl;
	}
};

class SubClassDemo2 : public ClassDemo
{
public:
	void public_function();
	void public_virtual_function() override;

	virtual ~SubClassDemo2()
	{
		std::cout << "类 SubClassDemo2 析构函数被调用" << std::endl;
	}
};

void ClassDemo::public_function()
{
	std::cout << "函数 SubClassDemo::public_function 被调用" << std::endl;
}

void ClassDemo::public_virtual_function()
{
	std::cout << "函数 ClassDemo::public_virtual_function 被调用" << std::endl;
}

void SubClassDemo::public_function()
{
	std::cout << "函数 SubClassDemo::public_function 被调用" << std::endl;
}

void SubClassDemo::public_virtual_function()
{
	std::cout << "函数 SubClassDemo::public_virtual_function 被调用" << std::endl;
}

void SubClassDemo2::public_function()
{
	std::cout << "函数 SubClassDemo2::public_function 被调用" << std::endl;
}

void SubClassDemo2::public_virtual_function()
{
	std::cout << "函数 SubClassDemo2::public_virtual_function 被调用" << std::endl;
}

void polymorphism_demo()
{
	[]
	{
		// 静态绑定（静态链接）：编译期间已经确定函数地址（即已经绑定到指针类型的函数上），无法使用多态。
		std::cout <<std::endl << "静态绑定示例：" << std::endl;
		ClassDemo* ptr_class_demo;
		SubClassDemo var_sub_class_demo;
		SubClassDemo2 var_sub_class_demo2;
		ptr_class_demo = &var_sub_class_demo;
		ptr_class_demo->public_function();
		ptr_class_demo = &var_sub_class_demo2;
		ptr_class_demo->public_function();
	}();

	[]
	{
		// 动态绑定：运行期间才能确定函数地址，可以调用不同派生类的函数实现。
		std::cout << std::endl << "动态绑定示例：" << std::endl;
		ClassDemo* ptr_class_demo;
		SubClassDemo var_sub_class_demo;
		SubClassDemo2 var_sub_class_demo2;
		ptr_class_demo = &var_sub_class_demo;
		ptr_class_demo->public_virtual_function();
		ptr_class_demo = &var_sub_class_demo2;
		ptr_class_demo->public_virtual_function();
	}();
}

int main(int argc, char* argv[])
{
	std::cout << "//////////// polymorphism_demo" << std::endl;
	polymorphism_demo();
}
