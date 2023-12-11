/************ 流程控制 ************/
namespace ControlDemo
{
    class Demo
    {
        void IfDemo()
        {
            System.Console.WriteLine("请输入 a 或 b");
            string varString = System.Console.ReadLine();
            if (varString.Equals("a"))
            {
                System.Console.WriteLine("您输入了 a");
            }
            else if (varString.Equals("b"))
            {
                System.Console.WriteLine("您输入了 b");
            }
            else
            {
                System.Console.WriteLine("您的输入无法识别");
            }
        }

        void ForDemo()
        {
            System.Console.WriteLine("请输入要遍历的范围");
            string varString = System.Console.ReadLine();
            int varInt = int.Parse(varString);

            System.Console.Write("开始遍历：");
            for (int i = 1; i <= varInt; i++)
            {
                System.Console.Write(i + "，");
            }
            System.Console.WriteLine();
        }

        void ForEachDemo()
        {
            System.Console.WriteLine("请输入要遍历的范围");
            string varString = System.Console.ReadLine();
            int varInt = int.Parse(varString);

            System.Console.Write("开始遍历：");
            foreach (int e in System.Linq.Enumerable.Range(1, varInt))
            {
                System.Console.Write(e + "，");
            }
            System.Console.WriteLine();

        }

        void WhileDemo()
        {
            System.Console.WriteLine("请输入要遍历的范围");
            string varString = System.Console.ReadLine();
            int varInt = int.Parse(varString);

            System.Console.Write("开始遍历：");
            while (varInt > 0)
            {
                System.Console.Write(varInt-- + "，");
            }
            System.Console.WriteLine();
        }
        static void Main(string[] args)
        {
            Demo demo = new Demo();
            System.Console.WriteLine("\n///////////// IfDemo");
            demo.IfDemo();

            System.Console.WriteLine("\n///////////// ForDemo");
            demo.ForDemo();

            System.Console.WriteLine("\n///////////// ForEachDemo");
            demo.ForEachDemo();

            System.Console.WriteLine("\n///////////// WhileDemo");
            demo.WhileDemo();
        }
    }
}