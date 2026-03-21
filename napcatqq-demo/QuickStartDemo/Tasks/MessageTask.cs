using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
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
    public class MessageTask : ITask
    {
        private static readonly Logger Log = LogManager.GetCurrentClassLogger();

        private readonly AppConfig _appConfig;
        private readonly IServiceScopeFactory _scopeFactory;

        private bool IsRunning { get; set; }
        private CancellationTokenSource _cts;
        private readonly ConcurrentDictionary<string, bool> _handledGroups = new ConcurrentDictionary<string, bool>();

        public MessageTask(AppConfig appConfig, IServiceScopeFactory scopeFactory)
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
                try
                {
                    while (!token.IsCancellationRequested)
                    {
                        using (var scope = _scopeFactory.CreateScope())
                        {
                            var groupRepository = scope.ServiceProvider.GetRequiredService<IGroupRepository>();
                            var messageRepository = scope.ServiceProvider.GetRequiredService<IMessageRepository>();
                            
                            var groups = groupRepository.QueryGroups();
                            foreach (var group in groups)
                            {
                                if (_handledGroups.ContainsKey(group.GroupId)) continue;
                                if (string.IsNullOrEmpty(group.NextMessageId)) group.NextMessageId = "0";

                                await Task.Delay(1000, token);
                                Log.Warn("[PullMessages][Start]: GroupId={0}, NextMessageId={1}", group.GroupId, group.NextMessageId);
                                var res = await HttpClientUtil.PostAsync(
                                    url: $"{_appConfig.HttpServerBaseUrl}/get_group_msg_history?access_token={_appConfig.Token}",
                                    body: $@"{{""group_id"":""{group.GroupId}"",""message_seq"":""{group.NextMessageId}"",""count"":20}}");
                                var resJson = JObject.Parse(res);
                                var messages = new List<MessageEntity>();
                                var nextMessageId = string.Empty;
                                if (resJson["data"] is JObject dataJson)
                                {
                                    if (dataJson["messages"] is JArray items && items.Count > 0)
                                    {
                                        nextMessageId = items[items.Count - 1]["message_id"]?.ToString();
                                        if (nextMessageId != group.NextMessageId)
                                        {
                                            group.NextMessageId = nextMessageId;
                                            groupRepository.UpdateGroup(group);

                                            foreach (var item in items)
                                            {
                                                messages.Add(new MessageEntity
                                                {
                                                    MessageId = item["message_id"]?.ToString(),
                                                    GroupId = item["group_id"]?.ToString(),
                                                    RawMessage = item["raw_message"]?.ToString(),
                                                    RawData = item.ToString()
                                                });
                                            }

                                            messageRepository.AddMessages(messages);
                                        }
                                    }
                                }

                                if (string.IsNullOrEmpty(nextMessageId) || nextMessageId == group.NextMessageId)
                                {
                                    _handledGroups.TryAdd(group.GroupId, true);
                                }
                                
                                Log.Warn("[PullMessages][End]: GroupId={0}, NextMessageId={1}, Count={2}", group.GroupId, group.NextMessageId, messages.Count);
                            }
                        }
                    }
                }
                catch (OperationCanceledException)
                {
                }
                catch (Exception ex)
                {
                    Log.Error(ex);
                }
                finally
                {
                    IsRunning = false;
                }
            }, token);
        }

        public void Stop()
        {
            _cts?.Cancel();
            IsRunning = false;
        }
    }
}