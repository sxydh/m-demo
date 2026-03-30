using System;
using System.Collections.Concurrent;
using System.Diagnostics;
using System.IO;
using System.Net.Http;
using System.Net.WebSockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using NLog;

namespace cschromecdp_demo.Services
{
    public class CdpServiceImpl : ICdpService
    {
        private static readonly Logger Log = LogManager.GetCurrentClassLogger();

        private readonly int _port;
        private readonly string _browserPath;
        private readonly string _userDataDir;

        private Process _browser;
        private ClientWebSocket _ws;
        private int _messageId;

        private readonly ConcurrentDictionary<string, TaskCompletionSource<JObject>> _waiters =
            new ConcurrentDictionary<string, TaskCompletionSource<JObject>>();

        public CdpServiceImpl(int port, string browserPath)
        {
            _port = port;
            _browserPath = browserPath;
            _userDataDir = Path.Combine(AppContext.BaseDirectory, "Temp", "ChromeUserDataDir", port.ToString());
            Log.Debug("Created. Port={0}, BrowserPath={1}, UserDataDir={2}", port, browserPath, _userDataDir);
        }

        public async Task StartAsync()
        {
            string wsUrl = null;
            if (Directory.Exists(_userDataDir))
            {
                Log.Debug("UserDataDir exists, checking if browser is still alive on port {0}.", _port);
                wsUrl = await TryGetWsUrlAsync();
                if (!string.IsNullOrEmpty(wsUrl))
                    Log.Info("Reusing existing browser on port {0}.", _port);
            }

            if (string.IsNullOrEmpty(wsUrl))
            {
                Directory.CreateDirectory(_userDataDir);
                Log.Info("Launching browser. Port={0}, Path={1}", _port, _browserPath);
                _browser = Process.Start(new ProcessStartInfo
                {
                    FileName = _browserPath,
                    Arguments = $@"--remote-debugging-port={_port} --user-data-dir=""{_userDataDir}"" --no-first-run --no-default-browser-check",
                    UseShellExecute = false
                });

                Log.Debug("Waiting for DevTools endpoint on port {0}.", _port);
                for (var i = 0; i < 20; i++)
                {
                    await Task.Delay(500);
                    wsUrl = await TryGetWsUrlAsync();
                    if (!string.IsNullOrEmpty(wsUrl))
                    {
                        Log.Debug("DevTools endpoint ready after {0} attempt(s).", i + 1);
                        break;
                    }

                    Log.Trace("Browser not ready, attempt={0}.", i + 1);
                }
            }

            if (string.IsNullOrEmpty(wsUrl))
            {
                Log.Error("Failed to get WebSocket debugger URL on port {0}.", _port);
                throw new InvalidOperationException($"Could not get browser WebSocket debugger URL on port {_port}.");
            }

            Log.Info("Connecting WebSocket. Url={0}", wsUrl);
            _ws = new ClientWebSocket();
            await _ws.ConnectAsync(new Uri(wsUrl), CancellationToken.None);
            Log.Info("WebSocket connected.");

            _ = ReceiveLoopAsync();
            await SendCommandAsync("Page.enable", null);

            var frameResult = await SendCommandAsync("Page.getFrameTree", null);
            var currentUrl = frameResult?["frameTree"]?["frame"]?["url"]?.ToString() ?? string.Empty;
            if (currentUrl.StartsWith("chrome://") || currentUrl.StartsWith("chrome-extension://"))
            {
                await SendCommandAsync("Page.navigate", new { url = "about:blank" });
            }
        }

        public async Task GotoAsync(string url, int timeout = 60000, bool isThrow = false)
        {
            Log.Info("GotoAsync. Url={0}, Timeout={1}", url, timeout);
            await SendCommandAsync("Page.navigate", new { url });
            await WaitForLoadAsync(timeout, isThrow);
        }

        public async Task WaitForLoadAsync(int timeout = 60000, bool isThrow = false)
        {
            Log.Debug("WaitForLoadAsync. Timeout={0}", timeout);
            var result = await SendCommandAsync("Runtime.evaluate", new { expression = "document.readyState", returnByValue = true });
            if (result?["result"]?["value"]?.ToString() == "complete")
            {
                Log.Debug("WaitForLoadAsync: already complete, returning immediately.");
                return;
            }

            await WaitForEventAsync("Page.loadEventFired", timeout, isThrow);
        }

        public async Task WaitForDomContentLoadedAsync(int timeout = 60000, bool isThrow = false)
        {
            Log.Debug("WaitForDomContentLoadedAsync. Timeout={0}", timeout);
            var result = await SendCommandAsync("Runtime.evaluate", new { expression = "document.readyState", returnByValue = true });
            var state = result?["result"]?["value"]?.ToString() ?? string.Empty;
            if (state == "interactive" || state == "complete")
            {
                Log.Debug("WaitForDomContentLoadedAsync: already {0}, returning immediately.", state);
                return;
            }

            await WaitForEventAsync("Page.domContentEventFired", timeout, isThrow);
        }

        public async Task<string> EvaluateAsync(string expression, int timeout = 60000, bool isThrow = false)
        {
            Log.Debug("EvaluateAsync. Expression={0}", expression);
            var result = await SendCommandAsync(
                method: "Runtime.evaluate",
                @params: new { expression, returnByValue = true },
                timeout: timeout,
                isThrow: isThrow);
            var value = result?["result"]?["value"]?.ToString() ?? string.Empty;
            Log.Debug("EvaluateAsync done. ResultLength={0}", value.Length);
            return value;
        }

        public async Task StopAsync()
        {
            Log.Info("StopAsync called.");

            foreach (var kv in _waiters)
                kv.Value.TrySetCanceled();
            _waiters.Clear();

            if (_ws != null && _ws.State == WebSocketState.Open)
            {
                try
                {
                    await _ws.CloseAsync(WebSocketCloseStatus.NormalClosure, "StopAsync", CancellationToken.None);
                    Log.Debug("WebSocket closed gracefully.");
                }
                catch (Exception ex)
                {
                    Log.Warn(ex, "Error closing WebSocket.");
                }
            }

            _ws?.Dispose();
            _ws = null;

            if (_browser != null && !_browser.HasExited)
            {
                try
                {
                    _browser.Kill();
                    Log.Debug("Browser process killed. PID={0}", _browser.Id);
                }
                catch (Exception ex)
                {
                    Log.Warn(ex, "Error killing browser process.");
                }
            }

            _browser?.Dispose();
            _browser = null;

            Log.Info("StopAsync done.");
        }

        private async Task<string> TryGetWsUrlAsync()
        {
            using (var http = new HttpClient())
            {
                http.Timeout = TimeSpan.FromSeconds(1);
                try
                {
                    var json = await http.GetStringAsync($"http://127.0.0.1:{_port}/json/list");
                    var targets = JArray.Parse(json);
                    foreach (var t in targets)
                    {
                        if (t["type"]?.ToString() != "page") continue;
                        var url = t["url"]?.ToString() ?? string.Empty;
                        if (url.StartsWith("chrome://") || url.StartsWith("chrome-extension://")) continue;
                        return t["webSocketDebuggerUrl"]?.ToString();
                    }

                    foreach (var t in targets)
                    {
                        if (t["type"]?.ToString() == "page")
                            return t["webSocketDebuggerUrl"]?.ToString();
                    }

                    return null;
                }
                catch
                {
                    return null;
                }
            }
        }

        private async Task<JObject> SendCommandAsync(string method, object @params, int timeout = 60000, bool isThrow = false)
        {
            var id = Interlocked.Increment(ref _messageId);
            var tcs = new TaskCompletionSource<JObject>();
            _waiters[id.ToString()] = tcs;

            var payload = JsonConvert.SerializeObject(new { id, method, @params });
            var bytes = Encoding.UTF8.GetBytes(payload);
            Log.Debug("SendCommand. Id={0}, Method={1}", id, method);
            await _ws.SendAsync(new ArraySegment<byte>(bytes), WebSocketMessageType.Text, true, CancellationToken.None);

            using (var cts = new CancellationTokenSource(timeout))
            {
                cts.Token.Register(() => tcs.TrySetCanceled());
                try
                {
                    return await tcs.Task;
                }
                catch
                {
                    if (isThrow)
                        throw;
                    return null;
                }
                finally
                {
                    _waiters.TryRemove(id.ToString(), out _);
                }
            }
        }

        private async Task WaitForEventAsync(string eventName, int timeout, bool isThrow)
        {
            if (!_waiters.TryGetValue(eventName, out var tcs))
            {
                tcs = new TaskCompletionSource<JObject>();
                _waiters[eventName] = tcs;
            }

            using (var cts = new CancellationTokenSource(timeout))
            {
                cts.Token.Register(() => tcs.TrySetCanceled());
                try
                {
                    await tcs.Task;
                }
                catch
                {
                    if (isThrow)
                        throw;
                }
                finally
                {
                    _waiters.TryRemove(eventName, out _);
                }
            }
        }

        private async Task ReceiveLoopAsync()
        {
            Log.Debug("ReceiveLoop started.");
            var buffer = new byte[65536];
            var sb = new StringBuilder();

            while (_ws != null && _ws.State == WebSocketState.Open)
            {
                sb.Clear();
                WebSocketReceiveResult result;
                do
                {
                    result = await _ws.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None);
                    if (result.MessageType == WebSocketMessageType.Close)
                    {
                        Log.Info("WebSocket closed by remote.");
                        return;
                    }

                    sb.Append(Encoding.UTF8.GetString(buffer, 0, result.Count));
                } while (!result.EndOfMessage);

                var raw = sb.ToString();
                Log.Trace("Received. Length={0}", raw.Length);

                try
                {
                    var obj = JObject.Parse(raw);
                    string key;
                    JObject payload;

                    if (obj["id"] != null)
                    {
                        key = obj["id"].ToString();
                        payload = obj["result"] as JObject;
                        var error = obj["error"];
                        if (error != null && _waiters.TryRemove(key, out var etcs))
                        {
                            etcs.SetException(new Exception(error["message"]?.ToString() ?? "CDP error"));
                            continue;
                        }
                    }
                    else
                    {
                        key = obj["method"]?.ToString();
                        payload = obj;
                    }

                    if (key != null && _waiters.TryRemove(key, out var tcs))
                        tcs.TrySetResult(payload);
                    else
                        Log.Trace("No waiter for key={0}, discarding.", key);
                }
                catch (Exception ex)
                {
                    Log.Error(ex, "Failed to parse WebSocket message.");
                }
            }

            Log.Info("ReceiveLoop exited. WsState={0}", _ws?.State.ToString() ?? "null");
        }
    }
}