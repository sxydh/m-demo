// ReSharper disable All
#include <iostream>
#include <string>

/************ 类前向声明（可选） ************/
// 前向声明主要是为了解决类之间循环依赖的问题
// 前向声明的类只能用于定义指针或引用
class ClassDemo;
class ClassDemo2;
class ClassDemo3;

class ClassDemo2
{
public:
	ClassDemo2()
	{
		std::cout << "构造函数 ClassDemo2() 被调用" << std::endl;
	}

	void public_function(ClassDemo arg_class_demo);

	~ClassDemo2()
	{
		std::cout << "析构函数 ClassDemo2 被调用" << std::endl;
	}
};

class ClassDemo3
{
public:
	ClassDemo3()
	{
		std::cout << "构造函数 ClassDemo3() 被调用" << std::endl;
	}

	void public_function(ClassDemo arg_class_demo);

	~ClassDemo3()
	{
		std::cout << "析构函数 ClassDemo3 被调用" << std::endl;
	}
};

/************ 类定义 ************/
class ClassDemo
{
	/************ 成员变量 ************/
	int var_int;
	std::string var_string;

	/************ 可见性修饰符 ************/
	// public：外部可见。
	// private：类内部、友元函数可见。类成员可见性的默认值。
	// protected：类内部、友元函数、子类可见。
public:
	int public_var_int;
	std::string public_var_string;
	mutable std::string mutable_var_string; // mutable 修饰的成员变量可以在常函数内修改
	static std::string static_var_string;

	/************ 构造函数（可选） ************/
	// 如果没有显示定义，编译器会自动定义一个空实现。
	ClassDemo()
	{
		std::cout << "构造函数 ClassDemo() 被调用" << std::endl;
		// this 是一个特殊的指针，指向当前对象实例。
		// 对象的成员函数都可以使用 this 指针
		this->public_var_int = 1;
		this->public_var_string = "Hello World!";
	}

	ClassDemo(int public_arg_int, std::string public_arg_string)
	{
		std::cout << "构造函数 ClassDemo(int, string) 被调用，参数为 public_arg_int：" << public_arg_int << "，public_arg_string：" << public_arg_string << std::endl;
		this->public_var_int = public_arg_int;
		this->public_var_string = public_arg_string;
	}

	/************ 拷贝构造函数（可选） ************/
	// 如果没有显示定义，编译器会自动创建一个浅拷贝构造函数（如果成员变量是指针，仅拷贝指针）。
	// 如果类成员包含指针变量，则必须显示定义。
	// 拷贝构造函数在以下情况会被调用：
	// 1、同类型对象赋值，例如：ClassDemo var_class_demo_2 = var_class_demo 或 ClassDemo var_class_demo_2 = ClassDemo(var_class_demo)等。
	// 2、作为函数实参进行值传递。
	// 3、作为函数返回值。
	ClassDemo(const ClassDemo& const_ref_class_demo)
	{
		std::cout << "拷贝构造函数 ClassDemo(ClassDemo&) 被调用，";
		std::cout << "&const_ref_class_demo = " << &const_ref_class_demo << "，";
		std::cout << "this = " << this << std::endl;
		this->public_var_int = const_ref_class_demo.public_var_int;
		this->public_var_string = const_ref_class_demo.public_var_string;
	}

	/************ 成员函数 ************/
	// 函数可以在类内部定义，也可以在外部定义。
	// 在类内部定义的函数会被编译器当作内联函数处理，即隐式内联。
	void public_function();

	void public_const_function() const; // const 修饰的函数是常函数，常函数内禁止修改成员变量。

	ClassDemo operator+(ClassDemo arg_class_demo); // 成员函数重载加号运算符，让两个对象实例可以实现 a + b 运算。

	static void public_static_function();

	/************ 析构函数（可选） ************/
	// 如果没有显示定义，编译器会自动定义一个空实现。
	// 对象被删除时执行
	~ClassDemo()
	{
		std::cout << "析构函数 ClassDemo 被调用，this = " << this << std::endl;
	}

	/************ 友元定义 ************/
	// 全局函数作为友元
	// 全局函数定义为类的友元后，可以访问类的 private 和 protected 成员。
	friend void global_function(ClassDemo arg_class_demo);

	// 其它类作为友元
	// 友元类可以访问类的 private 和 protected 成员
	// 友元类不是类的成员变量
	friend class ClassDemo2;

	// 其它类的成员函数作为友元
	// 此时其它类的成员函数可以访问类的 private 和 protected 成员
	friend void ClassDemo3::public_function(ClassDemo arg_class_demo);

private:
	int private_var_int = 1;
	std::string private_var_string = "Hello World!";

	void private_function();

protected:
	int protected_var_int;
	std::string protected_var_string;

	void protected_function();
} class_demo, class_demo_2; // 变量声明（可选）

/************ 类成员定义 ************/
std::string ClassDemo::static_var_string = "Hello World!";

void ClassDemo::public_function()
{
	std::cout << "函数 ClassDemo::public_function 被调用" << std::endl;
}

void ClassDemo::public_const_function() const
{
	std::cout << "函数 ClassDemo::public_const_function 被调用" << std::endl;
}

ClassDemo ClassDemo::operator+(ClassDemo arg_class_demo)
{
	std::cout << "函数 ClassDemo::operator+(ClassDemo) 被调用";
	ClassDemo var_class_demo;
	var_class_demo.public_var_int = this->public_var_int + arg_class_demo.public_var_int;
	var_class_demo.public_var_string = this->public_var_string + arg_class_demo.public_var_string;
	return var_class_demo;
}


void ClassDemo::public_static_function()
{
	std::cout << "函数 ClassDemo::public_static_function 被调用" << std::endl;
}

void ClassDemo::private_function()
{
	std::cout << "函数 ClassDemo::private_function 被调用" << std::endl;
}

void ClassDemo::protected_function()
{
	std::cout << "函数 ClassDemo::protected_function 被调用" << std::endl;
}

void ClassDemo2::public_function(ClassDemo arg_class_demo)
{
	std::cout << "函数 ClassDemo2::public_function 被调用，";
	std::cout << "ClassDemo2 是 ClassDemo 的友元，可以访问 ClassDemo 的私有成员：";
	std::cout << "arg_class_demo.private_var_int = " << arg_class_demo.private_var_int << "，";
	std::cout << "arg_class_demo.private_var_string = " << arg_class_demo.private_var_string << std::endl;
}

void ClassDemo3::public_function(ClassDemo arg_class_demo)
{
	std::cout << "函数 ClassDemo3::public_function 被调用，";
	std::cout << "ClassDemo3::public_function 是 ClassDemo 的友元，可以访问 ClassDemo 的私有成员：";
	std::cout << "arg_class_demo.private_var_int = " << arg_class_demo.private_var_int << "，";
	std::cout << "arg_class_demo.private_var_string = " << arg_class_demo.private_var_string << std::endl;
}

/************ 友元定义 ************/
void global_function(ClassDemo arg_class_demo)
{
	std::cout << "函数 global_function 被调用，";
	std::cout << "global_function 是类 ClassDemo 的友元，可以访问 ClassDemo 的私有成员：";
	std::cout << "private_var_int = " << arg_class_demo.private_var_int << "，";
	std::cout << "private_var_string = " << arg_class_demo.private_var_string;
	std::cout << std::endl;
}

/************ 类初始化 ************/
void class_init_demo()
{
	[]
	{
		// 按顺序初始化
		ClassDemo var_class_demo = {1, "Hello World!"};
		std::cout << "按顺序初始化后 var_class_demo 为：" << var_class_demo.public_var_int << "，" << var_class_demo.public_var_string << std::endl;
	}();

	[]
	{
		// 指定初始化
		ClassDemo var_class_demo;
		var_class_demo.public_var_int = 1;
		var_class_demo.public_var_string = "Hello World!";
		std::cout << "指定初始化后 var_class_demo 为：" << var_class_demo.public_var_int << "，" << var_class_demo.public_var_string << std::endl;
	}();

	[]
	{
		// 构造函数初始化
		ClassDemo var_class_demo(1, "Hello World!");
		std::cout << "构造函数初始化后 var_class_demo 为：" << var_class_demo.public_var_int << "，" << var_class_demo.public_var_string << std::endl;
	}();
}

/************ 类访问 ************/
void class_call_demo()
{
	// 访问静态成员
	ClassDemo::public_static_function();

	// 访问实例成员
	ClassDemo var_class_demo = {1, "Hello World!"};
	var_class_demo.public_function();
	var_class_demo.public_const_function();
	var_class_demo = var_class_demo + var_class_demo;
	std::cout << "加号运算符重载实现：var_class_demo = var_class_demo + var_class_demo，结果为：";
	std::cout << "public_var_int = " << var_class_demo.public_var_int << "，";
	std::cout << "public_var_string = " << var_class_demo.public_var_string << std::endl;

	ClassDemo2 var_class_demo2;
	var_class_demo2.public_function(var_class_demo);

	ClassDemo3 var_class_demo3;
	var_class_demo3.public_function(class_demo);
}

/************ 类指针 ************/
void class_as_pointer_demo()
{
	ClassDemo var_class_demo = {1, "Hello World!"};
	ClassDemo* ptr_class_demo = &var_class_demo;
	std::cout << "指针 ptr_class_demo 为：" << ptr_class_demo << std::endl;
	std::cout << "指针 ptr_class_demo 指向内容为：" << ptr_class_demo->public_var_int << "，" << ptr_class_demo->public_var_string << std::endl;
}

/************ 类数组 ************/
void class_as_array_demo()
{
	ClassDemo arr_class_demo[3] = {
		{1, "Hello World!"},
		{2, "Hello World!Hello World!"},
		{3, "Hello World!Hello World!Hello World!"}
	};
	std::cout << "开始遍历：" << std::endl;
	for (ClassDemo e : arr_class_demo)
	{
		std::cout << e.public_var_int << "，" << e.public_var_string << std::endl;
	}
}

/************ 类作为参数 ************/
void class_as_arg_demo(ClassDemo var_class_demo, ClassDemo* ptr_class_demo, ClassDemo& ref_class_demo)
{
	std::cout << "值传递 var_class_demo 为：" << var_class_demo.public_var_int << "，" << var_class_demo.public_var_string << std::endl;
	std::cout << "指针传递 ptr_class_demo 为：" << ptr_class_demo->public_var_int << "，" << ptr_class_demo->public_var_string << std::endl;
	std::cout << "引用传递 ref_class_demo 为：" << ref_class_demo.public_var_int << "，" << ref_class_demo.public_var_string << std::endl;
}

/************ 类作为返回值 ************/
ClassDemo class_as_return_value_demo()
{
	ClassDemo var_class_demo = {1, "Hello World!"};
	std::cout << "返回前地址：" << &var_class_demo << "，内容为：" << var_class_demo.public_var_int << "，" << var_class_demo.public_var_string << std::endl;
	return var_class_demo;
}

int main(int argc, char* argv[])
{
	std::cout << std::endl << "//////////// class_init_demo" << std::endl;
	class_init_demo();

	std::cout << std::endl << "//////////// class_call_demo" << std::endl;
	class_call_demo();

	std::cout << std::endl << "//////////// class_as_pointer_demo" << std::endl;
	class_as_pointer_demo();

	std::cout << std::endl << "//////////// class_as_array_demo" << std::endl;
	class_as_array_demo();

	[]
	{
		std::cout << std::endl << "//////////// class_as_arg_demo" << std::endl;
		ClassDemo var_class_demo = {1, "Hello World!"};
		ClassDemo& ref_class_demo = var_class_demo;
		class_as_arg_demo(var_class_demo, &var_class_demo, ref_class_demo);
	}();

	[]
	{
		std::cout << std::endl << "//////////// class_as_return_value_demo" << std::endl;
		ClassDemo var_class_demo = class_as_return_value_demo();
		var_class_demo.public_var_int *= 100;
		var_class_demo.public_var_string += var_class_demo.public_var_string;
		std::cout << "返回后地址：" << &var_class_demo << "，内容为：" << var_class_demo.public_var_int << "，" << var_class_demo.public_var_string << std::endl;
	}();
}
