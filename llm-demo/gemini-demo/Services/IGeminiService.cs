using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace gemini_demo.Services
{
    public interface IGeminiService
    {
        Task<string> GenerateContentAsync(string prompt, IEnumerable<(string role, string text)> history);
        Task GenerateContentStreamAsync(string prompt, IEnumerable<(string role, string text)> history, Func<string, Task> onChunk);
    }
}