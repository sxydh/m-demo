// ReSharper disable All
#include<iostream>
#include <fstream>
#include<string>

/************ cin ************/
// 控制台输入
std::string cin_demo()
{
	std::string line;
	getline(std::cin, line);
	std::cout << "您输入了：" << line << std::endl;
	return line;
}

/************ ofstream ************/
// 文件输出
void ofstream_demo(std::string arg_string)
{
	std::ofstream var_ofstream;
	var_ofstream.open("ofstream_demo.txt", std::ios::out);
	var_ofstream << arg_string;
	var_ofstream.close();
	std::cout << "成功输出到文件 ofstream_demo.txt" << std::endl;
}

/************ ifstream ************/
// 文件输入
void ifstream_demo()
{
	std::ifstream var_ifstream;
	var_ifstream.open("ofstream_demo.txt", std::ios::in);
	std::string line;
	std::cout << "读取文件 ofstream_demo.txt：";
	while (getline(var_ifstream, line))
	{
		std::cout << line;
	}
	std::cout << std::endl;
	var_ifstream.close();
}

int main()
{
	std::cout << std::endl << "//////////// cin_demo" << std::endl;
	std::string var_string = cin_demo();

	std::cout << std::endl << "//////////// ofstream_demo" << std::endl;
	ofstream_demo(var_string);

	std::cout << std::endl << "//////////// ifstream_demo" << std::endl;
	ifstream_demo();
}
