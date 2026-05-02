using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace claudecode_demo.Services
{
    public interface IClaudeCodeService
    {
        Task<string> GenerateContentAsync(string prompt, IEnumerable<(string role, string text)> history);
        Task GenerateContentStreamAsync(string prompt, IEnumerable<(string role, string text)> history, Func<string, Task> onChunk);
    }
}