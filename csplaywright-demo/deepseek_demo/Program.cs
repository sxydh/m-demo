using System;
using System.Diagnostics;
using System.IO;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using Microsoft.Playwright;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace deepseek_demo
{
    internal class Program
    {
        public static void Main(string[] _)
        {
            Task.Run(async () =>
            {
                try
                {
                    await InitContext(Console.Write);
                    while (true)
                    {
                        Console.WriteLine();
                        var question = Console.ReadLine();
                        await SendQuestion(question);
                    }
                }
                catch (Exception e)
                {
                    Console.WriteLine(e);
                }
            }).Wait();
        }

        private const string ChromePath = @"C:\Program Files\Google\Chrome\Application\chrome.exe";
        private const int ChromeRemoteDebuggingPort = 9222;

        // Chrome 默认的 User Data 路径
        private static readonly string SourceUserDataPath = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "Google", "Chrome", "User Data"
        );

        // Chrome 临时的 User Data 路径
        private static readonly string TempUserDataPath = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "_Temp", "Google", "Chrome", "User Data"
        );

        // DeepSeek 会话 ID 存储
        private static string _sessionId;
        private static readonly string SessionIdPath = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "_Temp", "deepseek.session.id"
        );

        private static IPage _page;
        private const string QuestionInputSelector = "//textarea";
        private const string QuestionSendSelector = "(//div[@role='button'])[last()]";

        private static async Task InitContext(AnswerHandler answerHandler)
        {
            // 复制 Chrome 默认 User Data 到临时 User Data
            CopyUserDataDirectory();
            // 关闭以前打开的 Chrome 进程
            KillChromeProcessByPort();
            // 启动 Chrome 实例
            using (var process = new Process())
            {
                process.StartInfo = new ProcessStartInfo
                {
                    FileName = ChromePath,
                    Arguments = $@"--headless --remote-debugging-port={ChromeRemoteDebuggingPort} --start-maximized --user-data-dir=""{TempUserDataPath}"" --profile-directory=""Default""",
                    WindowStyle = ProcessWindowStyle.Normal
                };
                process.Start();
                await Task.Delay(6000);
            }

            // 创建 Playwright 实例
            var playwright = await Playwright.CreateAsync();
            var browser = await playwright.Chromium.ConnectOverCDPAsync("http://localhost:9222");
            var context = browser.Contexts[0];
            _page = await context.NewPageAsync();

            // 创建 Chrome => Playwright 通信函数
            await _page.ExposeFunctionAsync("_send_message_to_csharp", (string rawMsg) =>
            {
                var answer = ParseRawMsg(_page, rawMsg);
                answerHandler(answer);
            });
            
            // 注入脚本到 DeepSeek
            await _page.RouteAsync("https://static.deepseek.com/chat/static/main.*", async route =>
            {
                try
                {
                    var res = await route.FetchAsync();
                    var resText = await res.TextAsync();

                    const string keyText = "async createSessionAndCompletion(e,t){";
                    resText = resText.Replace(keyText, $"{keyText}window._temp_service=this;");

                    const string keyText2 = "applyDeltaToMessage(e,t){";
                    resText = resText.Replace(keyText2, $"{keyText2}try{{_send_message_to_csharp(JSON.stringify([e,t]));}}catch(e){{}}");

                    await route.FulfillAsync(new RouteFulfillOptions
                    {
                        ContentType = "application/javascript",
                        Body = resText,
                    });
                }
                catch
                {
                    await route.ContinueAsync();
                }
            });

            // 进入 DeepSeek
            await _page.GotoAsync("https://chat.deepseek.com/");
            await _page.TryWaitFor(
                QuestionInputSelector,
                timeout: 36000,
                isVisible: true,
                isThrow: true,
                msg: "进入页面超时");

            // 读取以前的会话
            ReadSessionId();
        }

        private static async Task SendQuestion(string question)
        {
            await _page.Locator(QuestionInputSelector).FillAsync(question, new LocatorFillOptions { Timeout = 3000 });
            await Task.Delay(1000);
            await _page.Locator(QuestionSendSelector).ClickAsync(new LocatorClickOptions { Timeout = 9000 });
        }

        private delegate void AnswerHandler(string answer);

        private static void CopyUserDataDirectory()
        {
            // 只复制一次
            if (Directory.Exists(TempUserDataPath)) return;

            // 复制前需要关闭所有 Chrome 进程
            KillAllChromeProcesses();
            Directory.CreateDirectory(TempUserDataPath);
            CopyDirectory(SourceUserDataPath, TempUserDataPath);
        }

        private static void KillAllChromeProcesses()
        {
            Process[] chromeProcesses;
            var maxReties = 5;
            while ((chromeProcesses = Process.GetProcessesByName("chrome")).Length > 0 && maxReties-- > 0)
            {
                foreach (var process in chromeProcesses)
                {
                    try
                    {
                        process.Kill();
                        process.WaitForExit();
                    }
                    catch
                    {
                        // ignored
                    }
                }
            }

            if (chromeProcesses.Length > 0)
            {
                throw new Exception("Failed to kill all Chrome processes");
            }
        }

        private static void KillChromeProcessByPort()
        {
            try
            {
                var startInfo = new ProcessStartInfo
                {
                    FileName = "cmd.exe",
                    Arguments = $"/c netstat -ano | findstr :{ChromeRemoteDebuggingPort}",
                    UseShellExecute = false,
                    RedirectStandardOutput = true,
                    CreateNoWindow = true
                };

                using (var process = Process.Start(startInfo))
                {
                    if (process == null) return;

                    var output = process.StandardOutput.ReadToEnd();
                    process.WaitForExit();
                    var pattern = $@"^\s*TCP\s+\S+:{ChromeRemoteDebuggingPort}\s+\S+\s+LISTENING\s+(\d+)\s*$";
                    var matches = Regex.Matches(output, pattern, RegexOptions.Multiline);
                    if (matches.Count == 0)
                    {
                        return;
                    }

                    var pid = int.Parse(matches[0].Groups[1].Value);
                    var targetProcess = Process.GetProcessById(pid);
                    targetProcess.Kill();
                    targetProcess.WaitForExit();
                }
            }
            catch
            {
                throw new Exception("Failed to kill Chrome process by port");
            }
        }

        private static void CopyDirectory(string sourceDir, string targetDir)
        {
            using (var process = new Process())
            {
                process.StartInfo = new ProcessStartInfo
                {
                    FileName = "robocopy.exe",
                    Arguments = $@"""{sourceDir}"" ""{targetDir}"" /E /ZB /R:3 /W:2 /XJ",
                    UseShellExecute = false,
                    CreateNoWindow = true,
                    RedirectStandardOutput = false,
                    RedirectStandardError = false
                };
                process.Start();
                process.WaitForExit();
                if (process.ExitCode >= 8)
                {
                    throw new Exception($@"Failed to copy ""{sourceDir}"" to ""{targetDir}""");
                }
            }
        }

        private static string ParseRawMsg(IPage page, string rawMsg)
        {
            var retMsg = string.Empty;
            
            try
            {
                var jArray = JsonConvert.DeserializeObject<JArray>(rawMsg);
                var msgType = jArray[0]["op"]?.ToString();
                var msgValue = jArray[0]["value"]?.Value<object>();
                var msgValueStr = msgValue?.ToString();
                if (msgType == "APPEND")
                {
                    if (msgValue is JArray array)
                    {
                        retMsg = array[0]["content"]?.ToString();
                    }
                    else
                    {
                        retMsg = msgValueStr;
                    }
                }

                if (msgValueStr == "FINISHED")
                {
                    retMsg = "\n\n";
                }

                var sessionId = jArray[1]["chatSessionId"]?.ToString();
                if (sessionId != _sessionId && !string.IsNullOrEmpty(sessionId))
                {
                    // 删除以前的会话
                    var oldSessionId = _sessionId;
                    if (!string.IsNullOrEmpty(oldSessionId))
                    {
                        Task.Run(async () => { await DeleteDeepSeekSession(page, oldSessionId); });
                    }

                    SaveSessionId(sessionId);
                }
            }
            catch (Exception e)
            {
                Console.WriteLine($"Failed to handle raw message: {e.Message}");
            }
            
            return retMsg;
        }

        private static void ReadSessionId()
        {
            if (File.Exists(SessionIdPath))
            {
                _sessionId = File.ReadAllText(SessionIdPath);
            }
        }

        private static void SaveSessionId(string sessionId)
        {
            var deepSeekSessionIdDir = Path.GetDirectoryName(SessionIdPath);
            if (!string.IsNullOrEmpty(deepSeekSessionIdDir) && !Directory.Exists(deepSeekSessionIdDir))
            {
                Directory.CreateDirectory(deepSeekSessionIdDir);
            }

            _sessionId = sessionId;
            File.WriteAllText(SessionIdPath, _sessionId);
        }

        private static async Task DeleteDeepSeekSession(IPage page, string sessionId)
        {
            try
            {
                await page.EvaluateAsync($"(async () => {{ return await window._temp_service.deleteSession('{sessionId}'); }})();");
                await Task.Delay(3000);
            }
            catch (Exception e)
            {
                Console.WriteLine($"Failed to delete DeepSeek session: {e.Message}");
            }
        }
    }

    internal static class Utils
    {
        public static async Task TryWaitFor(
            this IPage page,
            string selector,
            long timeout = 60000,
            bool isVisible = false,
            bool isThrow = false,
            string msg = null)
        {
            try
            {
                await page.Locator(selector).WaitForAsync(
                    new LocatorWaitForOptions
                    {
                        State = isVisible ? WaitForSelectorState.Visible : WaitForSelectorState.Attached,
                        Timeout = timeout
                    });
            }
            catch (Exception ex)
            {
                if (isThrow)
                {
                    if (string.IsNullOrEmpty(msg))
                    {
                        throw;
                    }

                    throw new Exception(msg, ex);
                }
            }
        }
    }
}