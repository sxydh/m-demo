using Microsoft.Playwright;
using System.Threading;
using System.Threading.Tasks;

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

                    await page.GotoAsync("https://www.baidu.com");

                    await Task.Delay(5000);
                    await browser.CloseAsync();
                }
            }).Wait();
        }

    }
}
