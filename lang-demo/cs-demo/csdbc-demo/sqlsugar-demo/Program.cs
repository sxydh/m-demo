using System;
using System.Threading.Tasks;
using SqlSugar;

namespace sqlsugar_demo
{
    internal class Program
    {
        public static void Main(string[] _)
        {
            Task.Run(async () =>
            {
                var db = new SqlSugarClient(new ConnectionConfig
                {
                    DbType = DbType.Sqlite, 
                    ConnectionString = "DataSource=test.db",
                    IsAutoCloseConnection = true,   
                    InitKeyType = InitKeyType.Attribute  
                });
        
                db.Aop.OnLogExecuting = (sql, parameters) =>
                {
                    Console.WriteLine($"SQL: {sql}");
                };
                db.CodeFirst.InitTables(typeof(Product));
            
                var product1 = new Product
                {
                    Name = "测试商品1",
                    Price = 99.9m,
                    CreateTime = DateTime.Now
                };
        
                await db.Insertable(product1).ExecuteCommandIdentityIntoEntityAsync();
                Console.WriteLine($"ID: {product1.Id}");
            }).Wait();
        }
    }
    
    [SugarTable("Product")]
    public class Product
    {
        [SugarColumn(IsPrimaryKey = true, IsIdentity = true)]
        public int Id { get; set; }
    
        [SugarColumn(ColumnName = "name", Length = 200, IsNullable = false)]
        public string Name { get; set; }
    
        [SugarColumn(ColumnName = "price", ColumnDataType = "decimal(10,2)")]
        public decimal Price { get; set; }
    
        [SugarColumn(ColumnName = "create_time", IsNullable = false, DefaultValue = "CURRENT_TIMESTAMP")]
        public DateTime CreateTime { get; set; }
    }
    
}