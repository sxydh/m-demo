using NLog;
using SqlSugar;

namespace QuickStartDemo.Helpers
{
    public class DbContext
    {
        private static readonly Logger Log = LogManager.GetCurrentClassLogger();
        
        public SqlSugarClient Db { get; private set; }

        public DbContext(string dbPath)
        {
            Db = new SqlSugarClient(new ConnectionConfig
            {
                DbType = DbType.Sqlite, 
                ConnectionString = $"DataSource={dbPath}",
                IsAutoCloseConnection = true,   
                InitKeyType = InitKeyType.Attribute  
            });
        
            Db.Aop.OnLogExecuting = (sql, parameters) =>
            {
                Log.Debug("[SQLExecuting]: sql={0}", sql);
            };
        }
    }
}