using System.Threading.Tasks;

namespace cschromecdp_demo.Services
{
    public interface ICdpService
    {
        Task StartAsync();
        Task GotoAsync(string url, long timeout = 60000, bool isThrow = false, string msg = null);
        Task WaitForLoadAsync(long timeout = 60000, bool isThrow = false, string msg = null);
        Task WaitForDomContentLoadedAsync(long timeout = 60000, bool isThrow = false, string msg = null);
        Task<string> EvaluateAsync(string expression, long timeout = 60000, bool isThrow = false, string msg = null);
        Task<bool> WaitForAsync(string selector, long timeout = 60000, bool isThrow = false, string msg = null);
        Task FillAsync(string selector, string value, long timeout = 9000, bool isThrow = false, string msg = null);
        Task PressSequentiallyAsync(string selector, string value, long timeout = 9000, bool isThrow = false, string msg = null);
        Task<string> GetAttributeAsync(string selector, string name, long timeout = 9000, bool isThrow = false, string msg = null);
        Task ClickAsync(string selector, long timeout = 18000, bool isThrow = false, string msg = null);
        Task StopAsync();
    }
}