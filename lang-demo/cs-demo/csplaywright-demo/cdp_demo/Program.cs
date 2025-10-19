using System;
using System.Diagnostics;
using System.IO;
using System.Threading.Tasks;
using Microsoft.Playwright;

namespace cdp_demo
{
    internal class Program
    {
        private const string ChromePath = @"C:\Program Files\Google\Chrome\Application\chrome.exe";

        private static readonly string SourceUserDataPath = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "Google", "Chrome", "User Data"
        );

        private static readonly string TempUserDataPath = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "_Temp", "Google", "Chrome", "User Data"
        );

        public static void Main(string[] _)
        {
            Task.Run(async () =>
            {
                using (var playwright = await Playwright.CreateAsync())
                {
                    KillChromeProcesses();
                    CopyUserDataDirectory();

                    using (var process = new Process())
                    {
                        process.StartInfo = new ProcessStartInfo
                        {
                            FileName = ChromePath,
                            Arguments = $@"--remote-debugging-port=9222 --start-maximized --user-data-dir=""{TempUserDataPath}""",
                            WindowStyle = ProcessWindowStyle.Normal
                        };

                        process.Start();
                        await Task.Delay(6000);
                    }

                    var browser = await playwright.Chromium.ConnectOverCDPAsync("http://localhost:9222");
                    var context = browser.Contexts[0];

                    var page = await context.NewPageAsync();
                    await page.GotoAsync("https://www.baidu.com");

                    await Task.Delay(5000);
                    await browser.CloseAsync();
                }
            }).Wait();
        }

        private static void KillChromeProcesses()
        {
            var chromeProcesses = Process.GetProcessesByName("chrome");
            foreach (var process in chromeProcesses)
            {
                try
                {
                    process.Kill();
                    process.WaitForExit();
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Failed to kill Chrome process: {ex.Message}");
                }
            }
        }

        private static void CopyUserDataDirectory()
        {
            if (Directory.Exists(TempUserDataPath))
            {
                Directory.Delete(TempUserDataPath, true);
                Directory.CreateDirectory(TempUserDataPath);
            }

            CopyDirectory(SourceUserDataPath, TempUserDataPath);
        }

        private static void CopyDirectory(string sourceDir, string targetDir)
        {
            using (var process = new Process())
            {
                process.StartInfo = new ProcessStartInfo
                {
                    FileName = "robocopy.exe",
                    Arguments = $"{sourceDir} {targetDir} /E /R:1 /W:1",
                    UseShellExecute = false,
                    CreateNoWindow = true,
                    RedirectStandardOutput = false,
                    RedirectStandardError = false
                };
                process.Start();
                process.WaitForExit();
            }
        }
    }
}