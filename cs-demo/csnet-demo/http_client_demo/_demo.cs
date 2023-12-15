using System.Net.Http;
using System;

namespace HttpClientDemo
{
    internal class Demo
    {
        static void Init()
        {
            HttpClient client = new HttpClient();
            HttpResponseMessage response = null;
            try
            {
                client.BaseAddress = new Uri("http://192.168.174.1:8080");
                response = client.GetAsync("/").Result;
                Console.WriteLine($"服务器响应：{response.Content.ReadAsStringAsync().Result}");
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }
            finally
            {
                client.Dispose();
                response?.Dispose();
            }
        }

        static void Main(string[] args)
        {
            Init();
            Console.ReadKey();
        }
    }
}
