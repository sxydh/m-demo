using NLog;
using qqautomation_demo.Entities;
using SqlSugar;

namespace qqautomation_demo.Services
{
    public class DbService
    {
        private static readonly Logger Log = LogManager.GetCurrentClassLogger();

        public SqlSugarClient Db { get; }

        public DbService(string dbName)
        {
            Db = new SqlSugarClient(new ConnectionConfig
            {
                DbType = DbType.Sqlite,
                ConnectionString = $"DataSource={dbName}",
                IsAutoCloseConnection = true,
                InitKeyType = InitKeyType.Attribute
            });

            Db.Aop.OnLogExecuting = (sql, parameters) => { Log.Debug($"SQL: {sql}"); };
            Db.CodeFirst.InitTables<UiaPathCacheEntity>();
        }
    }
}