/************ 命名空间 ************/
// 命名空间可以是不连续的，即可以分散在不同的位置。
// 命名空间可以嵌套
namespace NamespaceDemo
{
    class ClassDemo
    {
        public int varInt = 1;
        public string varString = "Hello World!";
    }
}
namespace NamespaceDemo
{
    class ClassDemo2 { }
    class Demo
    {
        /************ 命名空间访问 ************/
        void NamespaceAccessDemo()
        {
            NamespaceDemo.ClassDemo classDemo = new NamespaceDemo.ClassDemo();
            System.Console.WriteLine("NamespaceDemo.ClassDemo classDemo = (" + classDemo.varInt + "，" + classDemo.varString + ")");
        }
        static void Main(string[] args)
        {
            Demo demo = new Demo();
            System.Console.WriteLine("\n///////////// NamespaceAccessDemo");
            demo.NamespaceAccessDemo();
        }
    }
}
