/************ 结构体 ************/
namespace StructDemo
{
    /************ 结构体定义 ************/
    // 结构体是值类型，存储在栈中。
    // 结构体不支持继承
    // 结构体成员的默认访问修饰符 private
    struct StructDemo
    {
        // 成员变量
        public int varInt; 
        public string varString;

        // 构造函数（可选）
        // 可以定义有参构造函数，但是不能定义无参构造函数，无参构造函数是自动创建的。
        public StructDemo(int varInt, string varString)
        {
            this.varInt = varInt;
            this.varString = varString;
        }

        // 成员方法
        public void MethodDemo()
        {
            System.Console.WriteLine("StructDemo.MethodDemo 被调用");
        }
    }

    class Demo
    {
        /************ 结构体实例访问 ************/
        void StructAccessDemo()
        {
            // 使用 new 关键字
            StructDemo structDemo = new StructDemo();
            structDemo.varInt = 1;
            structDemo.varString = "Hello World!";

            System.Console.WriteLine("structDemo.varInt = " + structDemo.varInt);
            System.Console.WriteLine("structDemo.varString = " + structDemo.varString);
            structDemo.MethodDemo();

            // 不使用 new 关键字
            StructDemo structDemo2;
            structDemo2.varInt = 1;
            structDemo2.varString = "Hello World!";

            System.Console.WriteLine();
            System.Console.WriteLine("structDemo2.varInt = " + structDemo2.varInt);
            System.Console.WriteLine("structDemo2.varString = " + structDemo2.varString);
            structDemo2.MethodDemo();
        }
        static void Main(string[] args)
        {
            Demo demo = new Demo();
            System.Console.WriteLine("\n///////////// StructAccessDemo");
            demo.StructAccessDemo();
        }
    }
}
