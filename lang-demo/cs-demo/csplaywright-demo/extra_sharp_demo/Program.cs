using PlaywrightExtraSharp.Models;
using PlaywrightExtraSharp.Plugins.ExtraStealth;
using PlaywrightExtraSharp;
using System.Threading.Tasks;
using Microsoft.Playwright;

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
                    .LaunchAsync(new BrowserTypeLaunchOptions()
                    {
                        Headless = false
                    });
                    var page = await playwrightExtra.NewPageAsync(options: null);

                    await page.GotoAsync("https://www.browserscan.net/bot-detection");

                    await Task.Delay(10000);
                }
            }).Wait();
        }
    }
}
