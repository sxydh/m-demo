using System.Threading.Tasks;

namespace cschromecdp_demo.Services
{
    public interface ICdpService
    {
        Task StartAsync();
        Task GotoAsync(string url, int timeout = 60000, bool isThrow = false);
        Task WaitForLoadAsync(int timeout = 60000, bool isThrow = false);
        Task WaitForDomContentLoadedAsync(int timeout = 60000, bool isThrow = false);
        Task<string> EvaluateAsync(string expression, int timeout = 60000, bool isThrow = false);
        Task StopAsync();
    }
}