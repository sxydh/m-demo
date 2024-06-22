using System;
using System.Data;
using System.Data.SQLite;
using System.IO;

namespace SqliteDemo
{
    internal class Program
    {
        /************ 创建连接 ************/
        SQLiteConnection CreateConnDemo()
        {
            string path = "Data Source=";
            path = path + Directory.GetCurrentDirectory();
            path = path + @"\sqlite_demo.sqlite";
            Console.WriteLine("数据库文件为：" + path);
            SQLiteConnection connection = new SQLiteConnection(path);
            return connection;
        }
        /************ 创建表 ************/
        void CreateTableDemo()
        {
            SQLiteConnection conn = CreateConnDemo();
            conn.Open();
            string sql = " create table if not exists app_log (log_id text primary key, ip text, op text, val text) ";
            SQLiteCommand command = new SQLiteCommand(sql, conn);
            int ret = command.ExecuteNonQuery();
            conn.Close();
            Console.WriteLine("创建表 app_log 返回：" + ret);
        }
        /************ 插入数据 ************/
        void InsertDemo()
        {
            SQLiteConnection conn = CreateConnDemo();
            conn.Open();
            string sql = " insert into app_log (log_id, ip, op, val) values (";
            sql = sql + DateTime.Now.ToString("yyyyMMddHHmmssffff") + ", ";
            sql = sql + "'127.0.0.1', ";
            sql = sql + "'INSERT', ";
            sql = sql + "'1') ";
            SQLiteCommand command = new SQLiteCommand(sql, conn);
            int ret = command.ExecuteNonQuery();
            conn.Close();
            Console.WriteLine("插入表 app_log 返回：" + ret);
        }
        /************ 查询数据 ************/
        void QueryDemo()
        {
            SQLiteConnection conn = CreateConnDemo();
            conn.Open();

            string sql = " select * from app_log ";
            SQLiteCommand command = new SQLiteCommand(sql, conn);
            SQLiteDataReader reader = command.ExecuteReader();
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

            conn.Close();
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
