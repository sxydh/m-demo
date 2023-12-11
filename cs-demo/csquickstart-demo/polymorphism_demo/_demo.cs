/************ 多态 ************/
// 多态基于抽象方法或虚函数实现
namespace PolymorphismDemo
{
    class ClassDemo
    {
        public virtual void Method()
        {
            System.Console.WriteLine("ClassDemo.Method 被调用");
        }
    }
    class SubClassDemo : ClassDemo
    {
        public override void Method()
        {
            System.Console.WriteLine("ClassDemo.Method 被调用");
        }
    }
    class SubClassDemo2 : ClassDemo
    {
        public override void Method()
        {
            System.Console.WriteLine("SubClassDemo2.Method 被调用");
        }
    }
    class Demo
    {
        /************ 多态访问 ************/
        void PolymorphismAccessDemo()
        {
            ClassDemo classDemo;
            classDemo = new SubClassDemo();
            classDemo.Method();

            classDemo = new SubClassDemo2();
            classDemo.Method();
        }
        static void Main(string[] args)
        {
            Demo demo = new Demo();
            System.Console.WriteLine("\n///////////// PolymorphismAccessDemo");
            demo.PolymorphismAccessDemo();
        }
    }
}
