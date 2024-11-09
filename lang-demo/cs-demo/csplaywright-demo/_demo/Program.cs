using System;
using System.Threading.Tasks;
using Microsoft.Playwright;

namespace _demo
{
    internal class Program
    {
        static void Main(string[] args)
        {
            Task.Run(async () =>
            {
                using (var playwright = await Playwright.CreateAsync())
                {
                    var launchOptions = new BrowserTypeLaunchOptions
                    {
                        Headless = false,
                        ExecutablePath = @"C:\Program Files\Google\Chrome\Application\chrome.exe"
                    };
                    var browser = await playwright.Chromium.LaunchAsync(launchOptions);
                    var page = await browser.NewPageAsync();

                    /* 生命周期回调 */
                    // 从上到下优先顺序
                    page.Request += (_, irequest) =>
                    {
                        var type = irequest.GetType();
                        Console.WriteLine($"Request: {type}");
                    };
                    await page.RouteAsync("**/*", async route =>
                    {
                        var type = route.GetType();
                        Console.WriteLine($"RouteAsync: {type}");
                        await route.ContinueAsync();
                    });
                    page.Response += (_, iresponse) =>
                    {
                        var type = iresponse.GetType();
                        Console.WriteLine($"Response: {type}");
                    };
                    page.DOMContentLoaded += (_, ipage) =>
                    {
                        var type = ipage.GetType();
                        Console.WriteLine($"DOMContentLoaded: {type}");
                    };
                    page.Load += (_, ipage) =>
                    {
                        var type = ipage.GetType();
                        Console.WriteLine($"Load: {type}");
                    };
                    page.Close += (_, ipage) =>
                    {
                        var type = ipage.GetType();
                        Console.WriteLine($"Close: {type}");
                    };

                    await page.GotoAsync("https://www.baidu.com");

                    await Task.Delay(5000);
                    await browser.CloseAsync();
                }
            }).Wait();
        }
    }
}