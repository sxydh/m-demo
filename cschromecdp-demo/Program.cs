using System;
using System.Diagnostics.CodeAnalysis;
using System.IO;
using System.Threading.Tasks;
using cschromecdp_demo.Services;
using cschromecdp_demo.Utils;

namespace cschromecdp_demo
{
    [SuppressMessage("ReSharper", "ClassNeverInstantiated.Global")]
    [SuppressMessage("ReSharper", "UnusedParameter.Global")]
    internal class Program
    {
        public static void Main(string[] args)
        {
            Task.Run(async () =>
            {
                var cdpService = new CdpService(
                    port: PortUtil.GetAvailablePort(),
                    browserPath: @"C:\Program Files\Google\Chrome\Application\chrome.exe");
                await cdpService.StartAsync();
                await cdpService.GotoAsync("https://www.browserscan.net/bot-detection");
                await cdpService.WaitForDomContentLoadedAsync();
                await cdpService.EvaluateAsync("alert('Hello World!');");
                await cdpService.StopAsync();
            }).GetAwaiter().GetResult();
        }
    }
}