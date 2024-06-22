/************ 枚举 ************/
namespace EnumDemo
{
    /************ 枚举定义 ************/
    enum Day { Sun, Mon, Tue, Wed, Thu, Fri, Sat };
    class Demo
    {
        /************ 枚举访问 ************/
        void EnumAccessDemo()
        {
            System.Console.WriteLine(Day.Sun);
            System.Console.WriteLine(Day.Mon);
            System.Console.WriteLine(Day.Tue);
            System.Console.WriteLine(Day.Wed);
            System.Console.WriteLine(Day.Thu);
            System.Console.WriteLine(Day.Fri);
            System.Console.WriteLine(Day.Sat);
        }
        static void Main(string[] args)
        {
            Demo demo = new Demo();
            System.Console.WriteLine("\n///////////// EnumAccessDemo");
            demo.EnumAccessDemo();
        }
    }
}
