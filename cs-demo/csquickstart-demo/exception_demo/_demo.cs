/************ 异常 ************/
namespace ExceptionDemo
{
    /************ 自定义异常 ************/
    class CustomExceptionDemo : System.Exception
    {
        public CustomExceptionDemo(string msg) : base(msg) { }
    }
    class Demo
    {
        /************ 异常访问 ************/
        void ExceptionAccessDemo()
        {
            try
            {
                int varInt = 1 / int.Parse("0");
            }
            catch (CustomExceptionDemo ex)
            {
                System.Console.WriteLine("catch 被调用：" + ex.ToString());
            }
            finally
            {
                System.Console.WriteLine("finally 被调用");
            }
        }
        static void Main(string[] args)
        {
            Demo demo = new Demo();
            System.Console.WriteLine("\n///////////// ExceptionAccessDemo");
            demo.ExceptionAccessDemo();
        }
    }
}
