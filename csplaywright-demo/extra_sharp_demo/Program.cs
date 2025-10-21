using System.Threading.Tasks;
using Microsoft.Playwright;
using PlaywrightExtraSharp;
using PlaywrightExtraSharp.Models;
using PlaywrightExtraSharp.Plugins.ExtraStealth;

namespace extra_sharp_demo
{
    internal class Program
    {
        static void Main(string[] _)
        {
            Task.Run(async () =>
            {
                // https://www.nuget.org/packages/PlaywrightExtraSharp
                using (var playwrightExtra = new PlaywrightExtra(BrowserTypeEnum.Chromium))
                {
                    await playwrightExtra
                    .Install()
                    .Use(new StealthExtraPlugin())
                    .LaunchAsync(new BrowserTypeLaunchOptions
                        {
                            Headless = false,
                            Args = new[] { "--start-maximized" }
                        },
                        contextOptions: new BrowserNewContextOptions
                        {
                            ViewportSize = ViewportSize.NoViewport
                        });
                    var page = await playwrightExtra.NewPageAsync(options: null);

                    await page.GotoAsync("https://www.browserscan.net/bot-detection");

                    await Task.Delay(10000);
                }
            }).Wait();
        }
    }
}
