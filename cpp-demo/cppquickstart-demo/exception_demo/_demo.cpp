// ReSharper disable All
#include <iostream>

/************ 异常处理 ************/
void throw_demo()
{
	try
	{
		throw std::invalid_argument("无效参数");
	}
	catch (std::exception e)
	{
		std::cout << "捕获异常：" << typeid(e).name() << " = " << e.what() << std::endl;
	}
}

/************ 自定义异常 ************/
class CustomExceptionDemo : public std::exception
{
private:
	std::string msg;

public:
	CustomExceptionDemo()
	{
		this->msg = "未知错误";
	}

	CustomExceptionDemo(std::string arg_string)
	{
		this->msg = arg_string;
	}

	virtual const char* what() const
	{
		return this->msg.c_str();
	}
};

void custom_exception_demo()
{
	try
	{
		throw CustomExceptionDemo();
	}
	catch (CustomExceptionDemo e)
	{
		std::cout << "捕获异常：" << typeid(e).name() << " = " << e.what() << std::endl;
	}
}

int main()
{
	std::cout << std::endl << "//////////// throw_demo" << std::endl;
	throw_demo();

	std::cout << std::endl << "//////////// custom_exception_demo" << std::endl;
	custom_exception_demo();
}
