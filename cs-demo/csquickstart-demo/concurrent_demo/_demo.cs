using System;
using System.Threading;
using System.Threading.Tasks;

namespace concurrent_demo
{
    class Demo
    {
        /************ lock 使用 ************/
        static void LockDemo()
        {
            object lockObject = new object();
            lock (lockObject)
            {
                Console.WriteLine($"LockDemo 持有锁的线程：{Thread.CurrentThread.ManagedThreadId}");
            }
        }

        /************ Task 使用 ************/
        static void TaskDemo()
        {
            Task.Run(() =>
            {
                Console.WriteLine($"TaskDemo 当前线程：{Thread.CurrentThread.ManagedThreadId}");
            });
        }

        /************ await 使用 ************/
        static async Task AwaitDemo()
        {
            await Task.Run(() =>
            {
                Console.WriteLine($"AwaitDemo 线程等待：{Thread.CurrentThread.ManagedThreadId}");
                long ts = DateTime.Now.Ticks;
                long gap = 2 * TimeSpan.TicksPerSecond;
                while (DateTime.Now.Ticks - ts < gap) ;
                Console.WriteLine($"AwaitDemo 线程结束：{Thread.CurrentThread.ManagedThreadId}");
            });
            // 可以有返回值
            string msg = await Task.Run(() =>
            {
                long ts = DateTime.Now.Ticks;
                long gap = 2 * TimeSpan.TicksPerSecond;
                while (DateTime.Now.Ticks - ts < gap) ;
                return $"AwaitDemo 线程返回：{new Random().Next(0, 10)}，{Thread.CurrentThread.ManagedThreadId}";
            });
            Console.WriteLine(msg);
        }

        /************ ThreadPool 使用 ************/
        static void ThreadPoolDemo()
        {
            ThreadPool.SetMaxThreads(20, 20);
            CountdownEvent countdown = new CountdownEvent(1);
            ThreadPool.QueueUserWorkItem(o =>
            {
                Console.WriteLine($"ThreadPoolDemo 当前线程：{Thread.CurrentThread.ManagedThreadId}");
                countdown.Signal();
            });
            countdown.Wait();
        }

        static void Main(string[] args)
        {
            Console.WriteLine("\n///////////// LockDemo");
            LockDemo();

            Console.WriteLine("\n///////////// TaskDemo");
            TaskDemo();

            Console.WriteLine("\n///////////// AwaitDemo");
            AwaitDemo().GetAwaiter().GetResult();

            Console.WriteLine("\n///////////// ThreadPoolDemo");
            ThreadPoolDemo();
        }
    }
}
