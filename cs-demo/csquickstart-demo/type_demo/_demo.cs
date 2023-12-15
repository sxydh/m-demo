/************ 数据类型 ************/
namespace TypeDemo
{
    class Demo
    {
        /************ 数值类型 ************/
        void ValueTypeDemo()
        {
            byte varByte = 1; // 8位
            bool varBool = false; // 8位
            char varChar = 'a'; // 16位
            short varShort = 1; // 16位
            int varInt = 1; // 32位
            float varFloat = 1.1F; // 32位
            double varDouble = 1.1D; // 64位
            long varLong = 1L; // 64位

            decimal varDecimal = 1M; // 128位

            // 无符号数值
            ushort varUshort = 1; // 16位
            uint varUint = 1; // 32位 
            ulong varUlong = 1; // 64位

            System.Console.WriteLine("类型 " + typeof(byte) + " 占用 " + sizeof(byte));
            System.Console.WriteLine("类型 " + typeof(bool) + " 占用 " + sizeof(bool));
            System.Console.WriteLine("类型 " + typeof(char) + " 占用 " + sizeof(char));
            System.Console.WriteLine("类型 " + typeof(short) + " 占用 " + sizeof(short));
            System.Console.WriteLine("类型 " + typeof(int) + " 占用 " + sizeof(int));
            System.Console.WriteLine("类型 " + typeof(float) + " 占用 " + sizeof(float));
            System.Console.WriteLine("类型 " + typeof(double) + " 占用 " + sizeof(double));
            System.Console.WriteLine("类型 " + typeof(long) + " 占用 " + sizeof(long));
            System.Console.WriteLine("类型 " + typeof(decimal) + " 占用 " + sizeof(decimal));
            System.Console.WriteLine("类型 " + typeof(ushort) + " 占用 " + sizeof(ushort));
            System.Console.WriteLine("类型 " + typeof(uint) + " 占用 " + sizeof(uint));
            System.Console.WriteLine("类型 " + typeof(ulong) + " 占用 " + sizeof(ulong));
        }

        /************ 类型转换 ************/
        void ConvertTypeDemo()
        {
            // 隐式转换
            int varInt = 1;
            long varLong = varInt;

            // 显示转换
            byte varByte = (byte)varInt;
        }

        /************ 引用类型 ************/
        void RefTypeDemo()
        {
            // 对象类型
            object varObject = new object();
            System.Console.WriteLine("varObject = " + varObject);

            // 动态类型
            // 运行时检查变量的类型
            dynamic varDynamic;
            varDynamic = 1;
            varDynamic = "Hello World!";
            varDynamic = new object();

            // 字符串类型
            string varString = "Hello World!";
            string varString2 = @"\m-demo\cs-demo"; // 使用 @ 可以对后续字符串转义，包括换行。
            System.Console.WriteLine($"varString = {varString}");
            System.Console.WriteLine($"varString2 = {varString2}");
        }

        /************ 可空类型 ************/
        void NullableTypeDemo()
        {
            // 标识符 ? 用于对 int、float、double 等无法直接赋值为 null 的数据类型进行 null 赋值。
            int? varInt = null;
            float? varFloat = null;
            double? varDouble = null;

            System.Console.WriteLine("varInt = " + varInt);
            System.Console.WriteLine("varFloat = " + varFloat);
            System.Console.WriteLine("varDouble = " + varDouble);
        }

        /************ 指针类型 ************/
        void PointerTypeDemo()
        {
            // C# 的指针类型与 C++ 类似
            // 不建议使用
        }

        /************ 数组类型 ************/
        void ArrayTypeDemo()
        {
            // 数组声明
            int[] arrInt;
            string[] arrString;

            // 数组初始化
            arrInt = new int[] { 1, 2, 3 };
            arrString = new string[] { "Hello", " ", "World" };

            // 数组声明并初始化
            float[] arrFloat = { 1, 1F, 2.2F, 3.3F };
            double[] arrDouble = { 1.1, 2.2, 3.3 };

            System.Console.WriteLine("arrInt = " + string.Join(",", arrInt));
            System.Console.WriteLine("arrString = " + string.Join(",", arrString));
            System.Console.WriteLine("arrFloat = " + string.Join(",", arrFloat));
            System.Console.WriteLine("arrDouble = " + string.Join(",", arrDouble));
        }

        static void Main(string[] args)
        {
            Demo demo = new Demo();
            System.Console.WriteLine("\n///////////// ValueTypeDemo");
            demo.ValueTypeDemo();

            System.Console.WriteLine("\n///////////// ConvertTypeDemo");
            demo.ConvertTypeDemo();

            System.Console.WriteLine("\n///////////// RefTypeDemo");
            demo.RefTypeDemo();

            System.Console.WriteLine("\n///////////// NullableTypeDemo");
            demo.NullableTypeDemo();

            System.Console.WriteLine("\n///////////// PointerTypeDemo");
            demo.PointerTypeDemo();

            System.Console.WriteLine("\n///////////// ArrayTypeDemo");
            demo.ArrayTypeDemo();
        }
    }
}
