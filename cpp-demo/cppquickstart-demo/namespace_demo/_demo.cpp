// ReSharper disable All
#include <iostream>
#include <string>

/************ 命名空间 ************/
// 命名空间用来解决同名冲突问题
// 命名空间可以是不连续的，即同一个命名空间可以分散在多个地方。

namespace namespace_01
{
	void function_demo()
	{
		std::cout << "namespace_01::function_demo 被调用" << std::endl;
	}
}

namespace namespace_01
{
	void function_demo2()
	{
		std::cout << "namespace_01::function_demo 被调用" << std::endl;
	}
}

namespace namespace_02
{
	void function_demo()
	{
		std::cout << "namespace_01::function_demo 被调用" << std::endl;
	}
}

int main(int argc, char* argv[])
{
	std::cout << std::endl << "//////////// namespace_01::function_demo" << std::endl;
	namespace_01::function_demo();

	std::cout << std::endl << "//////////// namespace_01::function_demo2" << std::endl;
	namespace_01::function_demo2();

	std::cout << std::endl << "//////////// namespace_02::function_demo" << std::endl;
	namespace_02::function_demo();
}
