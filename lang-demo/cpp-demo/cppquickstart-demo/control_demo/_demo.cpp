// ReSharper disable All
#include<iostream>


void if_demo()
{
	std::cout << "请输入任何整型" << std::endl;
	int var_int;
	std::cin >> var_int;
	if (var_int < 10)
	{
		std::cout << "您输入的值：" << var_int << " < 10" << std::endl;
	}
	else if (var_int < 100)
	{
		std::cout << "您输入的值：" << var_int << " < 100" << std::endl;
	}
	else
	{
		std::cout << "您输入的值：" << var_int << " 不在逻辑处理范围内" << std::endl;
	}
}

void swith_demo()
{
	// switch只支持整型
	std::cout << "请输入任何整型" << std::endl;
	int var_int;
	std::cin >> var_int;
	switch (var_int)
	{
	case 1:
		std::cout << "您输入的值：" << var_int << " 已匹配" << std::endl;
		break;
	case 2:
		std::cout << "您输入的值：" << var_int << " 已匹配" << std::endl;
		break;
	case 3:
		std::cout << "您输入的值：" << var_int << " 已匹配" << std::endl;
		break;
	default:
		std::cout << "您输入的值：" << var_int << " 未匹配" << std::endl;
	}
}

void for_demo()
{
	std::cout << "请输入要遍历的范围" << std::endl;
	int var_int;
	std::cin >> var_int;
	std::cout << "开始遍历：";
	int arr_int[20] = {};
	for (int i = 1; i <= var_int; i++)
	{
		if (i == 2)
		{
			std::cout << "遇到 2 跳过，";
			continue;
		}
		else if (i == 7)
		{
			std::cout << "遇到 7 中断";
			break;
		}
		else
		{
			std::cout << i << "，";
			arr_int[i - 1] = i;
		}
	}
	std::cout << std::endl;

	std::cout << "开始遍历：";
	for (int e : arr_int)
	{
		if (e == 0)
		{
			break;
		}
		std::cout << e << "，";
	}
	std::cout << std::endl;
}

void while_demo()
{
	std::cout << "请输入要遍历的范围" << std::endl;
	int var_int;
	std::cin >> var_int;
	std::cout << "开始遍历：";
	while (var_int > 0)
	{
		std::cout << var_int-- << "，";
	}
	std::cout << std::endl;
}

int main()
{
	std::cout << std::endl << "//////////// if_demo" << std::endl;
	if_demo();

	std::cout << std::endl << "//////////// swith_demo" << std::endl;
	swith_demo();

	std::cout << std::endl << "//////////// for_demo" << std::endl;
	for_demo();

	std::cout << std::endl << "//////////// while_demo" << std::endl;
	while_demo();
}
