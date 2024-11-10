using System.Threading.Tasks;
using Microsoft.Playwright;

namespace extension_demo
{
    internal class Program
    {
        static void Main(string[] _)
        {
            Task.Run(async () =>
            {
                using (var playwright = await Playwright.CreateAsync())
                {
                    /* 配置插件 */
                    // https://playwright.dev/docs/chrome-extensions
                    const string extensionPath = @"D:\Code\1-My\m-demo\ide-demo\chrome-demo\extension\manifest_v3\helloworld-demo";
                    var launchOptions = new BrowserTypeLaunchPersistentContextOptions
                    {
                        Headless = false,
                        Args = new[]
                        {
                            $"--disable-extensions-except={extensionPath}",
                            $"--load-extension={extensionPath}",
                            "--start-maximized"
                        },
                        ViewportSize = ViewportSize.NoViewport
                    };
                    var browser = await playwright.Chromium.LaunchPersistentContextAsync(string.Empty, launchOptions);
                    var page = await browser.NewPageAsync();

                    await page.GotoAsync("https://www.baidu.com");

                    await Task.Delay(5000);
                    await browser.CloseAsync();
                }
            }).Wait();
        }
    }
}