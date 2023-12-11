/************ 接口 ************/
namespace InterfaceDemo
{
    /************ 接口定义 ************/
    // 接口成员访问修饰符默认是 public
    interface InterfaceDemo
    {
        void Method();
    }
    /************ 接口继承 ************/
    interface InterfaceDemo2 : InterfaceDemo
    {
        void Method2();
    }
    /************ 接口实现 ************/
    class ClassDemo : InterfaceDemo, InterfaceDemo2
    {
        public void Method()
        {
            System.Console.WriteLine("ClassDemo.Method 被调用");
        }
        public void Method2()
        {
            System.Console.WriteLine("ClassDemo.Method2 被调用");
        }
    }
    class Demo
    {
        /************ 接口访问 ************/
        void InterfaceAccessDemo()
        {
            InterfaceDemo interfaceDemo = new ClassDemo();
            interfaceDemo.Method();

            InterfaceDemo2 interfaceDemo2 = new ClassDemo();
            interfaceDemo2.Method2();

        }
        static void Main(string[] args)
        {
            Demo demo = new Demo();
            System.Console.WriteLine("\n///////////// InterfaceAccessDemo");
            demo.InterfaceAccessDemo();
        }
    }
}
