using System.Net.Sockets;
using System.Net;
using System.Text;
using System;
using System.Threading;

namespace SocketServerDemo
{
    internal class Demo
    {

        void InitSocketServer()
        {
            Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            socket.Bind(new IPEndPoint(IPAddress.Any, 10006));
            socket.Listen(10);
            Console.WriteLine("[" + Thread.CurrentThread.ManagedThreadId + "] Server is running...");

            bool pool = ThreadPool.SetMaxThreads(10, 10);

            while (true)
            {
                // 阻塞接收客户端连接
                Socket conn = socket.Accept();
                Console.WriteLine("Client connected: " + conn.RemoteEndPoint.ToString());
                ThreadPool.QueueUserWorkItem(o => ConnHandler(conn));
            }
        }

        void ConnHandler(Socket conn)
        {
            while (true)
            {
                // first byte is the length of the message
                byte[] buffer = new byte[1];
                conn.Receive(buffer, 1, SocketFlags.None);
                int len = buffer[0];
                buffer = new byte[len];
                conn.Receive(buffer, len, SocketFlags.None); // 阻塞接收数据
                string msg = Encoding.UTF8.GetString(buffer, 0, len);
                Console.WriteLine("[" + Thread.CurrentThread.ManagedThreadId + "] Message received[" + conn.RemoteEndPoint.ToString() + "]: " + msg);

                if (conn.Poll(1000, SelectMode.SelectRead) && conn.Available == 0)
                {
                    Console.WriteLine("Client disconnected: " + conn.RemoteEndPoint.ToString());
                    break;
                }
            }
        }

        static void Main(string[] args)
        {
            Demo demo = new Demo();
            demo.InitSocketServer();
        }
    }
}
