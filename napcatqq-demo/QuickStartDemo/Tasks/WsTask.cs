using System;
using System.Collections.Generic;
using System.IO;
using System.Net.WebSockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using QuickStartDemo.Entities;
using QuickStartDemo.Helpers;
using QuickStartDemo.Repositories.Interfaces;
using Microsoft.Extensions.DependencyInjection;
using Newtonsoft.Json.Linq;
using NLog;

namespace QuickStartDemo.Tasks
{
    public class WsTask : ITask
    {
        private static readonly Logger Log = LogManager.GetCurrentClassLogger();

        private readonly AppConfig _appConfig;
        private readonly IServiceScopeFactory _scopeFactory;

        private bool IsRunning { get; set; }
        private CancellationTokenSource _cts;

        public WsTask(AppConfig appConfig, IServiceScopeFactory scopeFactory)
        {
            _appConfig = appConfig;
            _scopeFactory = scopeFactory;
        }

        public void Start()
        {
            if (IsRunning) return;
            IsRunning = true;
            _cts = new CancellationTokenSource();
            var token = _cts.Token;
            _ = Task.Run(async () =>
            {
                while (!token.IsCancellationRequested)
                {
                    try
                    {
                        using (var client = new ClientWebSocket())
                        {
                            Log.Info("[ClientWebSocket.ConnectAsync][Start]");
                            await client.ConnectAsync(new Uri($"{_appConfig.WsServerBaseUrl}?access_token={_appConfig.Token}"), token);
                            Log.Info("[ClientWebSocket.ConnectAsync][End]: Success");

                            var buffer = new byte[1024 * 8];
                            while (client.State == WebSocketState.Open && !token.IsCancellationRequested)
                            {
                                using (var ms = new MemoryStream())
                                {
                                    WebSocketReceiveResult result;
                                    do
                                    {
                                        result = await client.ReceiveAsync(new ArraySegment<byte>(buffer), token);
                                        if (result.MessageType == WebSocketMessageType.Close) break;
                                        ms.Write(buffer, 0, result.Count);
                                    } while (!result.EndOfMessage);

                                    if (result.MessageType == WebSocketMessageType.Close)
                                    {
                                        await client.CloseAsync(WebSocketCloseStatus.NormalClosure, "Receiver closing", CancellationToken.None);
                                        break;
                                    }

                                    ms.Seek(0, SeekOrigin.Begin);
                                    using (var reader = new StreamReader(ms, Encoding.UTF8))
                                    {
                                        var res = await reader.ReadToEndAsync();
                                        var resJson = JObject.Parse(res);
                                        if (resJson["message_id"] != null)
                                        {
                                            var message = new MessageEntity
                                            {
                                                MessageId = resJson["message_id"]?.ToString(),
                                                RawData = res
                                            };

                                            using (var scope = _scopeFactory.CreateScope())
                                            {
                                                var messageRepository = scope.ServiceProvider.GetRequiredService<IMessageRepository>();
                                                messageRepository.AddMessages(new List<MessageEntity> { message });
                                            }

                                            Log.Info("[ReceiveMessage]: MessageId={0}", message.MessageId);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    catch (OperationCanceledException)
                    {
                        break;
                    }
                    catch (Exception ex)
                    {
                        Log.Error(ex);
                        await Task.Delay(5000, token);
                    }
                }

                IsRunning = false;
            }, token);
        }

        public void Stop()
        {
            _cts?.Cancel();
            IsRunning = false;
        }
    }
}