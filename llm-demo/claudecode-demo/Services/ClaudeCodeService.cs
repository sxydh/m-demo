using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace claudecode_demo.Services
{
    public class ClaudeCodeService : IClaudeCodeService
    {
        private const string MsgApi = "/v1/messages";
        private readonly string _model;
        private readonly HttpClient _http;

        public ClaudeCodeService(string baseUrl, string authToken, string model = "claude-sonnet-4-6")
        {
            _http = new HttpClient { BaseAddress = new Uri(baseUrl) };
            _http.DefaultRequestHeaders.Add("Authorization", "Bearer " + authToken);
            _http.DefaultRequestHeaders.Add("anthropic-version", "2023-06-01");
            _model = model;
        }

        public async Task<string> GenerateContentAsync(string prompt, IEnumerable<(string role, string text)> history)
        {
            var sb = new StringBuilder();
            await GenerateContentStreamAsync(prompt, history, chunk =>
            {
                sb.Append(chunk);
                return Task.CompletedTask;
            });
            return sb.ToString();
        }

        public async Task GenerateContentStreamAsync(string prompt, IEnumerable<(string role, string text)> history, Func<string, Task> onChunk)
        {
            var body = BuildRequestBody(prompt, history);
            var response = await PostAsync(body);

            using (var stream = await response.Content.ReadAsStreamAsync())
            using (var reader = new StreamReader(stream))
            {
                while (!reader.EndOfStream)
                {
                    var line = await reader.ReadLineAsync();
                    if (line == null || !line.StartsWith("data:")) continue;

                    var data = line.Substring(5).Trim();
                    if (string.IsNullOrEmpty(data)) continue;

                    var evt = JObject.Parse(data);
                    if (evt["type"]?.ToString() == "content_block_delta" &&
                        evt["delta"]?["type"]?.ToString() == "text_delta")
                    {
                        await onChunk(evt["delta"]?["text"]?.ToString());
                    }
                }
            }
        }

        private object BuildRequestBody(string prompt, IEnumerable<(string role, string content)> history)
        {
            var messages = history
                .Select(h => new { h.role, h.content })
                .Concat(new[] { new { role = "user", content = prompt } })
                .ToList();
            return new { model = _model, max_tokens = 8096, stream = true, messages };
        }

        private Task<HttpResponseMessage> PostAsync(object body)
        {
            var json = JsonConvert.SerializeObject(body);
            var content = new StringContent(json, Encoding.UTF8, "application/json");
            return _http.PostAsync(MsgApi, content);
        }
    }
}