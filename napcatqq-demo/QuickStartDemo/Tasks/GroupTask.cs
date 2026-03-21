using System;
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
    public class GroupTask : ITask
    {
        private static readonly Logger Log = LogManager.GetCurrentClassLogger();

        private readonly AppConfig _appConfig;
        private readonly IServiceScopeFactory _scopeFactory;
        
        private bool IsRunning { get; set; }
        private CancellationTokenSource _cts;

        public GroupTask(AppConfig appConfig, IServiceScopeFactory scopeFactory)
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
                        Log.Warn("[PullGroups][Start]");
                        var res = await HttpClientUtil.PostAsync(
                            url: $"{_appConfig.HttpServerBaseUrl}/get_group_list?access_token={_appConfig.Token}",
                            body: "{}");
                        var resJson = JObject.Parse(res);
                        var groups = new List<GroupEntity>();
                        if (resJson["data"] is JArray items && items.Count > 0)
                        {
                            foreach (var item in items)
                            {
                                groups.Add(new GroupEntity
                                {
                                    GroupId = item["group_id"]?.ToString(),
                                    GroupName = item["group_name"]?.ToString(),
                                    RawData = item.ToString()
                                });
                            }

                            using (var scope = _scopeFactory.CreateScope())
                            {
                                var groupRepository = scope.ServiceProvider.GetRequiredService<IGroupRepository>();
                                groupRepository.AddGroups(groups);
                            }
                        }

                        Log.Warn("[PullGroups][End]: Count={0}", groups.Count);
                        await Task.Delay(180 * 1000, token);
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