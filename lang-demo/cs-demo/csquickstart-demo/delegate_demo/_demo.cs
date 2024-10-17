using System;

namespace delegate_demo
{
    public class _demo
    {
        /* 委托定义 */
        private delegate string GetMsg();

        /* 委托变量 */
        private readonly GetMsg _getMsg;

        private _demo(GetMsg getMsg)
        {
            _getMsg = getMsg;
        }

        private void SayHello()
        {
            Console.WriteLine(_getMsg());
        }

        public static void Main(string[] args)
        {
            var demo = new _demo(() => "Hello World");
            demo.SayHello();
        }
    }
}