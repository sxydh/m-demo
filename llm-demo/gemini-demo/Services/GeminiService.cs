using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace gemini_demo.Services
{
    public class GeminiService : IGeminiService
    {
        private const string Model = "gemini-2.0-flash";
        private const string BaseUrl = "https://generativelanguage.googleapis.com/v1beta/models/";

        private readonly string _apiKey;
        private readonly HttpClient _httpClient;

        public GeminiService(string apiKey)
        {
            _apiKey = apiKey;
            _httpClient = new HttpClient();
        }

        public async Task<string> GenerateContentAsync(string prompt, IEnumerable<(string role, string text)> history)
        {
            var url = $"{BaseUrl}{Model}:generateContent?key={_apiKey}";
            var body = BuildRequestBody(prompt, history);
            var content = new StringContent(JsonConvert.SerializeObject(body), Encoding.UTF8, "application/json");

            var response = await _httpClient.PostAsync(url, content);
            var json = await response.Content.ReadAsStringAsync();
            var obj = JObject.Parse(json);

            return obj["candidates"]?[0]?["content"]?["parts"]?[0]?["text"]?.ToString();
        }

        public async Task GenerateContentStreamAsync(string prompt, IEnumerable<(string role, string text)> history, Func<string, Task> onChunk)
        {
            var url = $"{BaseUrl}{Model}:streamGenerateContent?alt=sse&key={_apiKey}";
            var body = BuildRequestBody(prompt, history);
            var request = new HttpRequestMessage(HttpMethod.Post, url)
            {
                Content = new StringContent(JsonConvert.SerializeObject(body), Encoding.UTF8, "application/json")
            };

            var response = await _httpClient.SendAsync(request, HttpCompletionOption.ResponseHeadersRead);
            var stream = await response.Content.ReadAsStreamAsync();

            using (var reader = new StreamReader(stream))
            {
                string line;
                while ((line = await reader.ReadLineAsync()) != null)
                {
                    if (!line.StartsWith("data: ")) continue;
                    var data = line.Substring(6).Trim();
                    if (data == "[DONE]") break;

                    var obj = JObject.Parse(data);
                    var text = obj["candidates"]?[0]?["content"]?["parts"]?[0]?["text"]?.ToString();
                    if (!string.IsNullOrEmpty(text))
                        await onChunk(text);
                }
            }
        }

        private static object BuildRequestBody(string prompt, IEnumerable<(string role, string text)> history)
        {
            var contents = new JArray();
            if (history != null)
            {
                foreach (var (role, text) in history)
                {
                    contents.Add(new JObject
                    {
                        ["role"] = role,
                        ["parts"] = new JArray { new JObject { ["text"] = text } }
                    });
                }
            }

            contents.Add(new JObject
            {
                ["role"] = "user",
                ["parts"] = new JArray { new JObject { ["text"] = prompt } }
            });

            return new JObject { ["contents"] = contents };
        }
    }
}
