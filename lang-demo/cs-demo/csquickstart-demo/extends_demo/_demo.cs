/************ 继承 ************/
namespace ExtendsDemo
{
    class ClassDemo
    {
        private int varInt = 1;
        public ClassDemo()
        {
            System.Console.WriteLine("构造函数 ClassDemo() 被调用");
        }
        public void SetVarInt(int argInt)
        {
            this.varInt = argInt;
        }
        public int GetVarInt()
        {
            return this.varInt;
        }
        public void MethodDemo()
        {
            System.Console.WriteLine("ClassDemo.MethodDemo 被调用");
        }
    }
    class ClassDemo2 { }

    /************ 继承定义 ************/
    // 派生类继承基类成员
    // 不支持多继承
    // 子类实例化时，会自动先实例化基类。
    class SubClassDemo : ClassDemo
    {

        public SubClassDemo()
        {
            System.Console.WriteLine("构造函数 SubClassDemo() 被调用");
        }
        // 屏蔽基类成员函数
        // new 关键字可选
        public new void MethodDemo()
        {
            System.Console.WriteLine("SubClassDemo.MethodDemo 被调用");
            System.Console.WriteLine("手动调用基类成员函数：");
            base.MethodDemo();
        }
    }
    class Demo
    {
        /************ 继承实例化 ************/
        void ExtendsAccessDemo()
        {
            System.Console.WriteLine("实例化 SubClassDemo：");
            SubClassDemo subClassDemo = new SubClassDemo();
            System.Console.WriteLine("实力玩 SubClassDemo 完毕，继承自基类成员 SubClassDemo.varInt = " + subClassDemo.GetVarInt());

            System.Console.WriteLine();
            System.Console.WriteLine("调用子类 MethodDemo：");
            subClassDemo.MethodDemo();
        }
        static void Main(string[] args)
        {
            Demo demo = new Demo();
            System.Console.WriteLine("\n///////////// ExtendsAccessDemo");
            demo.ExtendsAccessDemo();
        }
    }
}
