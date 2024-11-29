using System;
using System.Threading.Tasks;
using Microsoft.Playwright;

namespace _demo
{
    internal class Program
    {
        static void Main()
        {
            Task.Run(async () =>
            {
                /* 浏览器初始化 */
                // https://playwright.dev/dotnet/docs/browsers
                // C:\Users\Administrator\AppData\Local\ms-playwright
                // 如果 ExecutablePath 显示指定了浏览器则可以不用初始化
                // var exitCode = Microsoft.Playwright.Program.Main(new[] { "install" });
                // if (exitCode != 0)
                // {
                //     throw new Exception($"Playwright exited with code {exitCode}");
                // }

                using (var playwright = await Playwright.CreateAsync())
                {
                    var launchOptions = new BrowserTypeLaunchOptions
                    {
                        Headless = false,
                        Args = new[] { "--start-maximized", "--auto-open-devtools-for-tabs" },
                        ExecutablePath = @"C:\Program Files\Google\Chrome\Application\chrome.exe",
                        Proxy = new Proxy
                        {
                            Server = "http://127.0.0.1:7890",
                        }
                    };
                    var browser = await playwright.Chromium.LaunchAsync(launchOptions);
                    var context = await browser.NewContextAsync(new BrowserNewContextOptions
                    {
                        ViewportSize = ViewportSize.NoViewport,
                        // 使用中间人代理时建议忽略 HTTPS 错误
                        IgnoreHTTPSErrors = true
                    });
                    var page = await context.NewPageAsync();

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
                    await page.RouteAsync("https://www.baidu.com/", async route =>
                    {
                        try
                        {
                            // 注意
                            // - page.EvaluateAsync 只有等页面加载后才能执行
                            // - route.FetchAsync 不会使用系统代理
                            // - route.FetchAsync 页面网络无日志

                            var headers = route.Request.Headers;
                            headers["demo"] = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss");
                            var res = await route.FetchAsync(new RouteFetchOptions
                            {
                                Headers = headers,
                                Timeout = 9000
                            });
                            var text = await res.TextAsync();
                            await route.FulfillAsync(new RouteFulfillOptions
                            {
                                Body = text
                            });
                        }
                        catch (Exception)
                        {
                            await route.ContinueAsync();
                        }
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

                    /* 打开目标页面 */
                    await page.GotoAsync("https://www.baidu.com/", new PageGotoOptions { Timeout = 10000 });
                    await page.WaitForLoadStateAsync(LoadState.Load, new PageWaitForLoadStateOptions { Timeout = 10000 });
                    await Task.Delay(3000);

                    /* 选择器 xpath */
                    // https://playwright.dev/dotnet/docs/locators#locate-by-css-or-xpath
                    // https://www.w3schools.com/xml/xpath_syntax.asp

                    // 从根元素开始查找
                    var divLocator = page.Locator("xpath=//div");
                    var divCount = await divLocator.CountAsync();
                    Console.WriteLine($"root divCount: {divCount}");

                    // 从当前元素开始查找
                    var firstDivLocator = divLocator.First.Locator("xpath=//div");
                    var firstDivCount = await firstDivLocator.CountAsync();
                    Console.WriteLine($"firstDiv divCount: {firstDivCount}");

                    // 指定属性
                    var navLocator = page.Locator("xpath=//div[@id='s-top-left']");
                    var navHtml = await navLocator.InnerHTMLAsync();
                    Console.WriteLine($"navHtml: {navHtml}");

                    // 属性包含指定值
                    var newsLocator = page.Locator("xpath=//a[contains(@href, 'http://news.baidu.com')]");
                    var news = await newsLocator.InnerTextAsync();
                    Console.WriteLine($"news: {news}");

                    // 内容包含指定值
                    var haoLocator = page.Locator("xpath=//a[contains(text(), 'hao123')]");
                    var haoText = await haoLocator.InnerTextAsync();
                    Console.WriteLine($"hao: {haoText}");

                    // 指定第几个
                    var firstInputLocator = page.Locator("xpath=(//input)[1]");
                    var firstInputValue = await firstInputLocator.GetAttributeAsync("value");
                    Console.WriteLine($"firstInputValue: {firstInputValue}");

                    /* 交互 click */
                    // 注意目标元素不在最前面是无法点击的
                    var settingsLocator = page.Locator("xpath=//span[@id='s-usersetting-top']");
                    await settingsLocator.ClickAsync();

                    await Task.Delay(5000);
                    await browser.CloseAsync();
                }
            }).Wait();
        }
    }
}