using Microsoft.Playwright;
using System.Threading.Tasks;

namespace _demo
{
    internal class Program
    {
        async static void Main(string[] args)
        {
            using (var playwright = await Playwright.CreateAsync())
            {
                var launchOptions = new BrowserTypeLaunchOptions
                {
                    Headless = false
                };
                var browser = await playwright.Chromium.LaunchAsync(launchOptions);
                var page = await browser.NewPageAsync();

                await page.GotoAsync("https://www.baidu.com");

                await Task.Delay(5000);
                await browser.CloseAsync();
            }
        }
    }
}
