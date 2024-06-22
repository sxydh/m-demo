using System.Net.Sockets;
using System.Net;
using System.Text;
using System;

namespace SocketServerDemo
{
    internal class Demo
    {
        void InitSocketClient()
        {
            Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            socket.Connect(new IPEndPoint(IPAddress.Parse("127.0.0.1"), 10006));
            Console.WriteLine("Server is running...");
            while (true)
            {
                Random random = new Random();
                string msg = random.Next(0, 100).ToString();

                // first byte is the length of the message
                byte[] buffer = new byte[1024];
                buffer[0] = (byte)msg.Length;
                Encoding.UTF8.GetBytes(msg).CopyTo(buffer, 1);
                socket.Send(buffer, msg.Length + 1, SocketFlags.None);
                Console.WriteLine("Message sent: " + msg);

                System.Threading.Thread.Sleep(50);
            }
        }
        static void Main(string[] args)
        {
            Demo demo = new Demo();
            demo.InitSocketClient();
        }
    }
}
