using System;
using qqautomation_demo.Services;

namespace qqautomation_demo
{
    internal class Program
    {
        public static void Main(string[] args)
        {
            var dbService = new DbService("qqautomation_demo.db");
            var uiaService = new UiaService(dbService);
            var qqAutoService = new QqAutoService(uiaService);
            qqAutoService.GetGroupList();
            Console.ReadLine();
        }
    }
}