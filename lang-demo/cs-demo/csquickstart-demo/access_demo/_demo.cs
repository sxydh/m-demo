/************ 访问修饰符 ************/
namespace AccessDemo
{
    class ClassDemo
    {
        // public：外部可见。
        public int publicInt = 1;
        // private：内部可见。类成员默认值。
        private int privateInt = 1;
        // protected：内部和子类可见。
        protected int protectedInt = 1;
        // internal：同一个程序集可见。
        internal int internalInt = 1;
        // protected internal：protected和internal的并集。
        protected internal int protectedInternalInt = 1;
    }
    class Demo
    {
        void AccessDemo()
        {
            ClassDemo classDemo = new ClassDemo();
            System.Console.WriteLine("classDemo.publicInt = " + classDemo.publicInt);
            System.Console.WriteLine("classDemo.privateInt 不可访问");
            System.Console.WriteLine("classDemo.protectedInt 不可访问");
            System.Console.WriteLine("classDemo.internalInt = " + classDemo.internalInt);
            System.Console.WriteLine("classDemo.protectedInternalInt = " + classDemo.protectedInternalInt);

        }
        static void Main(string[] args)
        {
            Demo demo = new Demo();
            System.Console.WriteLine("\n///////////// AccessDemo");
            demo.AccessDemo();
        }
    }
}
