/************ IO ************/
namespace IoDemo
{
    class Demo
    {
        /************ StreamWriter ************/
        void StreamWriterDemo()
        {
            // using 可以释放资源
            using (System.IO.StreamWriter sw = new System.IO.StreamWriter(System.IO.Directory.GetParent(System.Environment.CurrentDirectory).Parent.FullName + @"\StreamWriterDemo.txt"))
            {
                System.Console.WriteLine("请输入要保存的文字：");
                string line = System.Console.ReadLine();
                sw.WriteLine(line);
            }
        }
        /************ StreamReader ************/
        void StreamReaderDemo()
        {
            // using 可以释放资源
            using (System.IO.StreamReader sr = new System.IO.StreamReader(System.IO.Directory.GetParent(System.Environment.CurrentDirectory).Parent.FullName + @"\StreamWriterDemo.txt"))
            {
                string line;
                while ((line = sr.ReadLine()) != null)
                {
                    System.Console.WriteLine(line);
                }
            }
        }
        static void Main(string[] args)
        {
            Demo demo = new Demo();
            System.Console.WriteLine("\n///////////// StreamWriterDemo");
            demo.StreamWriterDemo();

            System.Console.WriteLine("\n///////////// StreamReaderDemo");
            demo.StreamReaderDemo();
        }
    }
}
