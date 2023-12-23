/************ 变量 ************/
namespace VarDemo
{
    class Demo
    {
        void VarAccessDemo()
        {
            /************ 变量声明 ************/
            int varInt;
            string varString;
            var varLong = 1L; // 隐式类型，但是声明时必须初始化。

            /************ 常量声明 ************/
            const int constInt = 1;
            const string constString = "Hello World!";

            /************ 变量初始化 ************/
            varInt = 1;
            varString = "Hello World!";

            System.Console.WriteLine("变量 varInt 为：" + varInt);
            System.Console.WriteLine("变量 varString 为：" + varString);
            System.Console.WriteLine("变量 varLong 为：" + varLong);
            System.Console.WriteLine("常量 constInt 为：" + constInt);
            System.Console.WriteLine("常量 constString 为：" + constString);
        }

        static void Main(string[] args)
        {
            Demo demo = new Demo();
            System.Console.WriteLine("\n///////////// VarAccessDemo");
            demo.VarAccessDemo();
        }
    }
}
