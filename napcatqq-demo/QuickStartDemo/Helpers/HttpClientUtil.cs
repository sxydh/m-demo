using System;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;

namespace QuickStartDemo.Helpers
{
    public class HttpClientUtil
    {
        private static readonly HttpClient HttpClient = new HttpClient
        {
            Timeout = TimeSpan.FromSeconds(30)
        };

        /// <summary>
        /// 发起异步 GET 请求
        /// </summary>
        /// <exception cref="HttpRequestException">当响应状态码非 2xx 时抛出</exception>
        public static async Task<string> GetAsync(string url)
        {
            using (var response = await HttpClient.GetAsync(url))
            {
                response.EnsureSuccessStatusCode();
                return await response.Content.ReadAsStringAsync();
            }
        }

        /// <summary>
        /// 发起异步 POST 请求 (JSON 格式)
        /// </summary>
        /// <exception cref="HttpRequestException">当响应状态码非 2xx 时抛出</exception>
        public static async Task<string> PostAsync(string url, string body)
        {
            using (var content = new StringContent(body, Encoding.UTF8, "application/json"))
            {
                using (var response = await HttpClient.PostAsync(url, content))
                {
                    response.EnsureSuccessStatusCode();
                    return await response.Content.ReadAsStringAsync();
                }
            }
        }
    }
}