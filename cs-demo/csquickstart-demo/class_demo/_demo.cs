/************ 类 ************/
namespace ClassDemo
{
    /************ 类定义 ************/
    // 类是引用类型，存储在堆上。
    // 类的默认访问修饰符 internal，成员的默认访问修饰符 private。
    class ClassDemo
    {
        /************ 成员变量 ************/
        private int varInt = 1;
        private string varString = "Hello World!";
        // 静态成员变量
        public static int staticInt = 1;

        /************ 构造函数 ************/
        // 无参构造函数
        public ClassDemo() { }

        // 有参构造函数
        public ClassDemo(int argInt, string argString)
        {
            this.varInt = argInt;
            this.varString = argString;
        }

        /************ 成员方法 ************/
        public void SetVarInt(int argInt)
        {
            this.varInt = argInt;
        }

        public int GetVarInt()
        {
            return this.varInt;
        }

        public void SetVarString(string argString)
        {
            this.varString = argString;
        }

        public string GetVarString()
        {
            return this.varString;
        }

        // 静态成员方法
        public static void StaticMethodDemo()
        {
            System.Console.WriteLine("ClassDemo.StaticMethodDemo 被调用");
        }

        /************ 运算符重载 ************/
        // 重载加法
        public static ClassDemo operator +(ClassDemo a, ClassDemo b)
        {
            ClassDemo classDemo = new ClassDemo();
            classDemo.varInt = a.varInt + b.varInt;
            classDemo.varString = a.varString + b.varString;
            return classDemo;
        }

        /************ 析构函数 ************/
        ~ClassDemo()
        {
            System.Console.WriteLine("析构函数 ~ClassDemo 被调用");
        }
    }

    class Demo
    {
        /************ 类实例访问 ************/
        void ClassAccessDemo()
        {
            ClassDemo classDemo = new ClassDemo();
            classDemo.SetVarInt(1);
            classDemo.SetVarString("Hello World!");
            System.Console.WriteLine("ClassDemo.varInt = " + classDemo.GetVarInt());
            System.Console.WriteLine("ClassDemo.varString = " + classDemo.GetVarString());
        }
        /************ 类静态成员访问 ************/
        void ClassStaticAccessDemo()
        {
            ClassDemo.StaticMethodDemo();
        }
        /************ 重载运算符访问 ************/
        void OperatorPlusDemo()
        {
            ClassDemo a = new ClassDemo(1, "Hello World!");
            ClassDemo b = new ClassDemo(1, "Hello World!");
            ClassDemo c = a + b;
            System.Console.WriteLine("a+b=c varInt = " + c.GetVarInt());
            System.Console.WriteLine("a+b=c varString = " + c.GetVarString());
        }
        static void Main(string[] args)
        {
            Demo demo = new Demo();
            System.Console.WriteLine("\n///////////// ClassAccessDemo");
            demo.ClassAccessDemo();

            System.Console.WriteLine("\n///////////// ClassStaticAccessDemo");
            demo.ClassStaticAccessDemo();

            System.Console.WriteLine("\n///////////// OperatorPlusDemo");
            demo.OperatorPlusDemo();
        }
    }
}
