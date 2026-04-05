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
    public class CdpService : ICdpService
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

        public CdpService(int port, string browserPath)
        {
            _port = port;
            _browserPath = browserPath;
            _userDataDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                "SPD", "ChromeUserDataDir", port.ToString());
            Log.Debug($"Created. Port={port}, BrowserPath={browserPath}, UserDataDir={_userDataDir}");
        }

        public async Task StartAsync()
        {
            string wsUrl = null;
            if (Directory.Exists(_userDataDir))
            {
                Log.Debug($"UserDataDir exists, checking if browser is still alive on port {_port}.");
                wsUrl = await TryGetWsUrlAsync();
                if (!string.IsNullOrEmpty(wsUrl))
                    Log.Info($"Reusing existing browser on port {_port}.");
            }

            if (string.IsNullOrEmpty(wsUrl))
            {
                Directory.CreateDirectory(_userDataDir);
                Log.Info($"Launching browser. Port={_port}, Path={_browserPath}");
                _browser = Process.Start(new ProcessStartInfo
                {
                    FileName = _browserPath,
                    Arguments = $@"--remote-debugging-port={_port} --user-data-dir=""{_userDataDir}"" --no-first-run --no-default-browser-check --start-maximized",
                    UseShellExecute = false
                });

                Log.Debug($"Waiting for DevTools endpoint on port {_port}.");
                for (var i = 0; i < 20; i++)
                {
                    await Task.Delay(500);
                    wsUrl = await TryGetWsUrlAsync();
                    if (!string.IsNullOrEmpty(wsUrl))
                    {
                        Log.Debug($"DevTools endpoint ready after {i + 1} attempt(s).");
                        break;
                    }

                    Log.Trace($"Browser not ready, attempt={i + 1}.");
                }
            }

            if (string.IsNullOrEmpty(wsUrl))
            {
                Log.Error($"Failed to get WebSocket debugger URL on port {_port}.");
                throw new InvalidOperationException($"Could not get browser WebSocket debugger URL on port {_port}.");
            }

            Log.Info($"Connecting WebSocket. Url={wsUrl}");
            _ws = new ClientWebSocket();
            await _ws.ConnectAsync(new Uri(wsUrl), CancellationToken.None);
            Log.Info("WebSocket connected.");

            _ = ReceiveLoopAsync();
            await SendCommandAsync(method: "Page.enable", @params: null);

            var frameResult = await SendCommandAsync(method: "Page.getFrameTree", @params: null);
            var currentUrl = frameResult?["frameTree"]?["frame"]?["url"]?.ToString() ?? string.Empty;
            if (currentUrl.StartsWith("chrome://") || currentUrl.StartsWith("chrome-extension://"))
            {
                await SendCommandAsync(method: "Page.navigate", @params: new { url = "about:blank" });
            }
        }

        public async Task GotoAsync(string url, long timeout = 60000, bool isThrow = false, string msg = null)
        {
            try
            {
                Log.Info($"GotoAsync. Url={url}, Timeout={timeout}, Context={msg}");
                var sw = Stopwatch.StartNew();
                await SendCommandAsync(method: "Page.navigate", @params: new { url }, timeout: timeout);
                var remainingTimeout = (long)Math.Max(0, timeout - sw.Elapsed.TotalMilliseconds);
                await WaitForLoadAsync(timeout: remainingTimeout, isThrow: isThrow);
            }
            catch (Exception ex)
            {
                Log.Error($"GotoAsync Failed. Url={url}, Error={ex.Message}");
                if (isThrow) throw new Exception(msg ?? ex.Message, ex);
            }
        }

        public async Task WaitForLoadAsync(long timeout = 60000, bool isThrow = false, string msg = null)
        {
            try
            {
                Log.Debug($"WaitForLoadAsync. Timeout={timeout}");
                var sw = Stopwatch.StartNew();
                var result = await SendCommandAsync(
                    method: "Runtime.evaluate",
                    @params: new { expression = "document.readyState", returnByValue = true },
                    timeout: timeout);
                if (result?["result"]?["value"]?.ToString() == "complete")
                {
                    Log.Debug("WaitForLoadAsync: already complete, returning immediately.");
                    return;
                }

                var remainingTimeout = (long)Math.Max(0, timeout - sw.Elapsed.TotalMilliseconds);
                await WaitForEventAsync(eventName: "Page.loadEventFired", timeout: remainingTimeout);
            }
            catch (Exception ex)
            {
                Log.Error($"WaitForLoadAsync Failed. Error={ex.Message}");
                if (isThrow) throw new Exception(msg ?? ex.Message, ex);
            }
        }

        public async Task WaitForDomContentLoadedAsync(long timeout = 60000, bool isThrow = false, string msg = null)
        {
            try
            {
                Log.Debug($"WaitForDomContentLoadedAsync. Timeout={timeout}");
                var sw = Stopwatch.StartNew();
                var result = await SendCommandAsync(
                    method: "Runtime.evaluate",
                    @params: new { expression = "document.readyState", returnByValue = true },
                    timeout: timeout);
                var state = result?["result"]?["value"]?.ToString() ?? string.Empty;
                if (state == "interactive" || state == "complete")
                {
                    Log.Debug($"WaitForDomContentLoadedAsync: already {state}, returning immediately.");
                    return;
                }

                var remainingTimeout = (long)Math.Max(0, timeout - sw.Elapsed.TotalMilliseconds);
                await WaitForEventAsync(eventName: "Page.domContentEventFired", timeout: remainingTimeout);
            }
            catch (Exception ex)
            {
                Log.Error($"WaitForDomContentLoadedAsync Failed. Error={ex.Message}");
                if (isThrow) throw new Exception(msg ?? ex.Message, ex);
            }
        }

        public async Task<string> EvaluateAsync(string expression, long timeout = 60000, bool isThrow = false, string msg = null)
        {
            try
            {
                Log.Debug($"EvaluateAsync. Expression={expression}");
                var result = await SendCommandAsync(
                    method: "Runtime.evaluate",
                    @params: new { expression, returnByValue = true, awaitPromise = true },
                    timeout: timeout);
                var value = result?["result"]?["value"]?.ToString() ?? string.Empty;
                Log.Debug($"EvaluateAsync done. ResultLength={value.Length}");
                return value;
            }
            catch (Exception ex)
            {
                Log.Error($"EvaluateAsync Failed. Error={ex.Message}");
                if (isThrow) throw new Exception(msg ?? ex.Message, ex);
                return null;
            }
        }

        public async Task<bool> WaitForAsync(string selector, long timeout = 60000, bool isThrow = false, string msg = null)
        {
            try
            {
                Log.Debug($"WaitForAsync. Selector={selector}, Timeout={timeout}");
                var expression = $"document.querySelector(`{selector}`) !== null";

                var sw = Stopwatch.StartNew();
                while (true)
                {
                    var remaining = (long)Math.Max(0, timeout - sw.Elapsed.TotalMilliseconds);
                    if (remaining <= 0) break;

                    var result = await SendCommandAsync(
                        method: "Runtime.evaluate",
                        @params: new { expression, returnByValue = true },
                        timeout: remaining);
                    if (result?["result"]?["value"]?.ToObject<bool>() == true)
                    {
                        Log.Debug($"WaitForAsync: found. Selector={selector}");
                        return true;
                    }

                    remaining = (long)Math.Max(0, timeout - sw.Elapsed.TotalMilliseconds);
                    if (remaining <= 0) break;
                    await Task.Delay(TimeSpan.FromMilliseconds(Math.Min(500, remaining)));
                }

                Log.Debug($"WaitForAsync: timed out. Selector={selector}");
                if (isThrow)
                    throw new TimeoutException($"Selector not found: {selector}");
            }
            catch (Exception ex)
            {
                Log.Error($"WaitForAsync Failed. Error={ex.Message}");
                if (isThrow) throw new Exception(msg ?? ex.Message, ex);
            }

            return false;
        }

        public async Task FillAsync(string selector, string value, long timeout = 9000, bool isThrow = false, string msg = null)
        {
            try
            {
                Log.Debug($"FillAsync. Selector={selector}");
                var expression = $@"
(() => {{
    var el = document.querySelector(`{selector}`);
    if (!el) return false;
    var nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value') || Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, 'value');
    if (nativeInputValueSetter) nativeInputValueSetter.set.call(el, `{value}`);
    else el.value = `{value}`;
    el.dispatchEvent(new Event('input', {{ bubbles: true }}));
    el.dispatchEvent(new Event('change', {{ bubbles: true }}));
    return true;
}})();";
                var result = await SendCommandAsync(
                    method: "Runtime.evaluate",
                    @params: new { expression, returnByValue = true },
                    timeout: timeout);
                if (result?["result"]?["value"]?.ToObject<bool>() != true)
                {
                    Log.Debug($"FillAsync: selector not found. Selector={selector}");
                    if (isThrow)
                        throw new Exception($"Selector not found: {selector}");
                }
                else
                {
                    Log.Debug($"FillAsync done. Selector={selector}");
                }
            }
            catch (Exception ex)
            {
                Log.Error($"FillAsync Failed. Error={ex.Message}");
                if (isThrow) throw new Exception(msg ?? ex.Message, ex);
            }
        }

        public async Task PressSequentiallyAsync(string selector, string value, long timeout = 9000, bool isThrow = false, string msg = null)
        {
            try
            {
                Log.Debug($"PressSequentiallyAsync. Selector={selector}");
                var focusExpr = $@"
(() => {{
    var el = document.querySelector(`{selector}`);
    if (!el) return false;
    el.focus();
    return true;
}})();";
                var sw = Stopwatch.StartNew();
                var remaining = (long)Math.Max(0, timeout - sw.Elapsed.TotalMilliseconds);
                var result = await SendCommandAsync(
                    method: "Runtime.evaluate",
                    @params: new { expression = focusExpr, returnByValue = true },
                    timeout: remaining);
                if (result?["result"]?["value"]?.ToObject<bool>() != true)
                {
                    Log.Debug($"PressSequentiallyAsync: selector not found. Selector={selector}");
                    if (isThrow)
                        throw new Exception($"Selector not found: {selector}");
                    return;
                }

                foreach (var ch in value)
                {
                    var text = ch.ToString();

                    remaining = (long)Math.Max(0, timeout - sw.Elapsed.TotalMilliseconds);
                    if (remaining <= 0)
                        throw new TimeoutException("PressSequentiallyAsync timed out during typing.");
                    await SendCommandAsync(
                        method: "Input.dispatchKeyEvent",
                        @params: new { type = "keyDown", text },
                        timeout: remaining);

                    remaining = (long)Math.Max(0, timeout - sw.Elapsed.TotalMilliseconds);
                    if (remaining <= 0)
                        throw new TimeoutException("PressSequentiallyAsync timed out during typing.");
                    await SendCommandAsync(
                        method: "Input.dispatchKeyEvent",
                        @params: new { type = "keyUp", text },
                        timeout: remaining);
                }

                Log.Debug($"PressSequentiallyAsync done. Selector={selector}");
            }
            catch (Exception ex)
            {
                Log.Error($"PressSequentiallyAsync Failed. Error={ex.Message}");
                if (isThrow) throw new Exception(msg ?? ex.Message, ex);
            }
        }

        public async Task<string> GetAttributeAsync(string selector, string name, long timeout = 9000, bool isThrow = false, string msg = null)
        {
            try
            {
                Log.Debug($"GetAttributeAsync. Selector={selector}, Name={name}");
                var expression = $"(document.querySelector(`{selector}`) || {{}}).getAttribute(`{name}`)";
                var result = await SendCommandAsync(
                    method: "Runtime.evaluate",
                    @params: new { expression, returnByValue = true },
                    timeout: timeout);
                var value = result?["result"]?["value"]?.ToString();
                if (value == null)
                {
                    Log.Debug($"GetAttributeAsync: null result. Selector={selector}, Name={name}");
                    if (isThrow)
                        throw new Exception($"Attribute '{name}' not found on selector: {selector}");
                }
                else
                {
                    Log.Debug($"GetAttributeAsync done. Value={value}");
                }

                return value;
            }
            catch (Exception ex)
            {
                Log.Error($"GetAttributeAsync Failed. Error={ex.Message}");
                if (isThrow) throw new Exception(msg ?? ex.Message, ex);
            }

            return null;
        }

        public async Task ClickAsync(string selector, long timeout = 18000, bool isThrow = false, string msg = null)
        {
            try
            {
                Log.Debug($"ClickAsync. Selector={selector}");
                var expression = $@"
(() => {{
    var el = (`{selector}`.startsWith('//') || `{selector}`.startsWith('(//'))
        ? document.evaluate(`{selector}`, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue
        : document.querySelector(`{selector}`);
    if (!el) return false;
    el.click();
    return true;
}})();";
                var result = await SendCommandAsync(
                    method: "Runtime.evaluate",
                    @params: new { expression, returnByValue = true },
                    timeout: timeout);
                if (result?["result"]?["value"]?.ToObject<bool>() != true)
                {
                    Log.Debug($"ClickAsync: selector not found. Selector={selector}");
                    if (isThrow)
                        throw new Exception($"Selector not found: {selector}");
                }
                else
                {
                    Log.Debug($"ClickAsync done. Selector={selector}");
                }
            }
            catch (Exception ex)
            {
                Log.Error($"ClickAsync Failed. Error={ex.Message}");
                if (isThrow) throw new Exception(msg ?? ex.Message, ex);
            }
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
                    Log.Debug($"Browser process killed. PID={_browser.Id}");
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

        private async Task<JObject> SendCommandAsync(string method, object @params, long timeout = 60000)
        {
            var id = Interlocked.Increment(ref _messageId);
            var tcs = new TaskCompletionSource<JObject>();
            _waiters[id.ToString()] = tcs;

            var payload = JsonConvert.SerializeObject(new { id, method, @params });
            var bytes = Encoding.UTF8.GetBytes(payload);
            Log.Debug($"SendCommand. Id={id}, Method={method}");
            await _ws.SendAsync(new ArraySegment<byte>(bytes), WebSocketMessageType.Text, true, CancellationToken.None);

            using (var cts = new CancellationTokenSource(TimeSpan.FromMilliseconds(timeout)))
            {
                cts.Token.Register(() =>
                {
                    var errorMsg = $"Command execution timed out. Method: {method}, Id: {id}, Timeout: {timeout}ms";
                    tcs.TrySetException(new TimeoutException(errorMsg));
                });
                try
                {
                    return await tcs.Task;
                }
                finally
                {
                    _waiters.TryRemove(id.ToString(), out _);
                }
            }
        }

        private async Task WaitForEventAsync(string eventName, long timeout)
        {
            var tcs = _waiters.GetOrAdd(eventName, _ => new TaskCompletionSource<JObject>());
            using (var cts = new CancellationTokenSource(TimeSpan.FromMilliseconds(timeout)))
            {
                cts.Token.Register(() =>
                {
                    var errorMsg = $"Wait for event timed out. Event: {eventName}, Timeout: {timeout}ms";
                    tcs.TrySetException(new TimeoutException(errorMsg));
                });
                try
                {
                    await tcs.Task;
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
                Log.Trace($"Received. Length={raw.Length}");

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
                        Log.Trace($"No waiter for key={key}, discarding.");
                }
                catch (Exception ex)
                {
                    Log.Error(ex, "Failed to parse WebSocket message.");
                }
            }

            Log.Info($"ReceiveLoop exited. WsState={_ws?.State.ToString()}");
        }
    }
}