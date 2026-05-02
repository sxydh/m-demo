using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using gemini_demo.Services;

namespace gemini_demo
{
    internal class Program
    {
        public static void Main(string[] args)
        {
            Task.Run(async () =>
            {
                var apiKey = Environment.GetEnvironmentVariable("GEMINI_API_KEY");
                var service = new GeminiService(apiKey);

                var history = new List<(string role, string text)>
                {
                    ("user", "我的名字叫小明"),
                    ("model", "好的，我记住了，你的名字是小明。")
                };

                Console.WriteLine("=== GenerateContentAsync ===");
                var result = await service.GenerateContentAsync("我的名字是什么？", history);
                Console.WriteLine(result);

                Console.WriteLine("=== GenerateContentStreamAsync ===");
                await service.GenerateContentStreamAsync("再说一遍我的名字", history, chunk =>
                {
                    Console.Write(chunk);
                    return Task.CompletedTask;
                });
            }).GetAwaiter().GetResult();
            Console.ReadKey();
        }
    }
}