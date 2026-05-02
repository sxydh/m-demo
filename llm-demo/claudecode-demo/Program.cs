using System;
using System.Collections.Generic;
using claudecode_demo.Services;

namespace claudecode_demo
{
    internal class Program
    {
        public static void Main(string[] args)
        {
            var baseUrl = Environment.GetEnvironmentVariable("ANTHROPIC_BASE_URL");
            var authToken = Environment.GetEnvironmentVariable("ANTHROPIC_AUTH_TOKEN");
            var service = new ClaudeCodeService(baseUrl, authToken);
            var history = new List<(string role, string text)>
            {
                ("user", "你好，我叫张三，记住我的名字！"),
                ("assistant", "好的，我记住了，你叫张三。")
            };

            // 非流式
            var reply = service.GenerateContentAsync("我叫什么名字？", history).Result;
            Console.WriteLine(reply);

            // 流式
            service.GenerateContentStreamAsync("我叫什么名字？", history, chunk =>
            {
                Console.Write(chunk);
                return System.Threading.Tasks.Task.CompletedTask;
            }).Wait();
        }
    }
}