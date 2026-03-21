using System;
using System.IO;
using QuickStartDemo.Entities;
using QuickStartDemo.Helpers;
using QuickStartDemo.Repositories.Implementations;
using QuickStartDemo.Repositories.Interfaces;
using QuickStartDemo.Tasks;
using Microsoft.Extensions.DependencyInjection;
using NLog;

namespace QuickStartDemo
{
    internal class Program
    {
        private static readonly Logger Log = LogManager.GetCurrentClassLogger();

        public static void Main(string[] args)
        {
            Log.Info("Register components...");
            var services = new ServiceCollection();
            services.AddSingleton<AppConfig, AppConfig>();
            services.AddScoped(sp =>
            {
                var dbPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "QuickStartDemo.db");
                var dbContext = new DbContext(dbPath);
                dbContext.Db.CodeFirst.InitTables(typeof(GroupEntity));
                dbContext.Db.CodeFirst.InitTables(typeof(MessageEntity));
                return dbContext;
            });
            services.AddScoped<IGroupRepository, GroupRepository>();
            services.AddScoped<IMessageRepository, MessageRepository>();
            
            services.AddSingleton<ITask, GroupTask>();
            services.AddSingleton<ITask, MessageTask>();
            services.AddSingleton<ITask, WsTask>();

            var serviceProvider = services.BuildServiceProvider();
            var tasks = serviceProvider.GetServices<ITask>();
            foreach (var task in tasks) 
            {
                task.Start();
            }
            
            Log.Info("Press any key to end...");
            Console.ReadLine();
        }
    }
}