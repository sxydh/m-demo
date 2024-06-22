using Dapper;
using Microsoft.Data.Sqlite;
using System.IO;

namespace SqlcipherDemo
{
    internal class Program
    {
        /************ 创建连接 ************/
        SqliteConnection CreateConnDemo()
        {
            var dataSource = Directory.GetCurrentDirectory() + @"\sqlcipher_demo.sqlite";
            var password = "123";
            var connString = new SqliteConnectionStringBuilder()
            {
                Mode = SqliteOpenMode.ReadWriteCreate,
                Password = password,
                DataSource = dataSource
            }.ToString();
            Console.WriteLine("connectionString = " + connString);
            return new SqliteConnection(connString);
        }

        /************ 创建表 ************/
        void CreateTableDemo()
        {
            using var conn = CreateConnDemo();
            conn.Open();
            var sql = " create table if not exists app_log (log_id text primary key, ip text, op text, val text) ";
            var ret = conn.Execute(sql);
            Console.WriteLine("创建表 app_log 返回：" + ret);
        }

        /************ 插入数据 ************/
        void InsertDemo()
        {
            using var conn = CreateConnDemo();
            conn.Open();
            var sql = " insert into app_log (log_id, ip, op, val) values (";
            sql = sql + DateTime.Now.ToString("yyyyMMddHHmmssffff") + ", ";
            sql = sql + "'127.0.0.1', ";
            sql = sql + "'INSERT', ";
            sql = sql + "'1') ";
            var ret = conn.Execute(sql);
            Console.WriteLine("插入表 app_log 返回：" + ret);
        }

        /************ 查询数据 ************/
        void QueryDemo()
        {
            using var conn = CreateConnDemo();
            conn.Open();

            var sql = " select * from app_log ";
            var reader = conn.ExecuteReader(sql);
            Console.WriteLine("开始遍历：");
            while (reader.Read())
            {
                int count = reader.FieldCount;
                int i = -1;
                while (++i < count)
                {
                    Console.Write(reader[i] + "，");
                }
                Console.WriteLine();
            }
        }

        static void Main(string[] args)
        {
            try
            {
                Program program = new Program();
                Console.WriteLine("\n///////////// CreateConnDemo");
                program.CreateConnDemo();

                Console.WriteLine("\n///////////// CreateTableDemo");
                program.CreateTableDemo();

                Console.WriteLine("\n///////////// InsertDemo");
                program.InsertDemo();

                Console.WriteLine("\n///////////// QueryDemo");
                program.QueryDemo();
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
            Console.ReadLine();
        }
    }

}