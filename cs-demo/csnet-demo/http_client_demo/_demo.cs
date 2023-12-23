using System.Net.Http;
using System;

namespace HttpClientDemo
{
    internal class Demo
    {
        static void GetDemo()
        {
            HttpClient client = new HttpClient();
            HttpResponseMessage response = null;
            try
            {
                client.BaseAddress = new Uri("http://192.168.174.1:10006");
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

        static void PostDemo(string body)
        {
            HttpClient client = new HttpClient();
            HttpResponseMessage response = null;
            try
            {
                client.BaseAddress = new Uri("http://192.168.174.1:10006");
                response = client.PostAsync("/", new StringContent(body, System.Text.Encoding.UTF8)).Result;
                Console.WriteLine($"服务器响应：{response.Content.ReadAsStringAsync().Result}");
            }
            finally
            {
                client.Dispose();
                response?.Dispose();
            }
        }

        static void Main(string[] args)
        {
            Console.WriteLine("\n///////////// GetDemo");
            GetDemo();

            Console.WriteLine("\n///////////// PostDemo");
            PostDemo("Hello World!");

            Console.ReadKey();
        }
    }
}
