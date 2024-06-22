/************ 运算符 ************/
namespace OperatorDemo
{
    class Demo
    {
        /************ 基础运算符 ************/
        void OperatorDemo()
        {
            // 算数运算符
            int varInt = 1 + 1;
            // 关系运算符
            bool varBool = 1 > 2;
            // 逻辑运算符
            if (true)
            {
            }
            // 位运算符
            long varLong = 1L << 2;
            // 条件运算符
            byte varByte = true ? 1 : 2;
            // 其它运算符
            System.Console.WriteLine("sizeof(int) = " + sizeof(int));
            System.Console.WriteLine("typeof(int) = " + typeof(int));
        }

        static void Main(string[] args)
        {
            Demo demo = new Demo();
            System.Console.WriteLine("\n///////////// OperatorDemo");
            demo.OperatorDemo();
        }
    }
}
