// ReSharper disable All
#include <iostream>

/************ 枚举 ************/
// 枚举定义
enum Day
{
	// enum 值只能是整型，默认从0开始，然后按序编号。
	Monday,
	Tuesday = 12,
	Wednesday = 13,
	Thursday = 14,
	Friday = 15,
	Saturday = 16,
	Sunday
};

// 枚举访问
void enum_access()
{
	Day var_day = Monday;
	Day var_day2 = Tuesday;
	Day var_day3 = Sunday;
	Day var_dayx = Day(100);
	std::cout << var_day << std::endl;
	std::cout << var_day2 << std::endl;
	std::cout << var_day3 << std::endl;
	std::cout << var_dayx << std::endl;

	// 输出是：
	// 0
	// 12
	// 17
	// 100
}

int main(int argc, char* argv[])
{
	std::cout << std::endl << "//////////// enum_access" << std::endl;
	enum_access();
}
