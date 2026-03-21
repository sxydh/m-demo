using System;
using System.ComponentModel.DataAnnotations.Schema;
using SqlSugar;

namespace QuickStartDemo.Entities
{
    [Table("t_group")]
    public class GroupEntity
    {
        [SugarColumn(IsPrimaryKey = true, IsIdentity = true)]
        public int Id { get; set; }
        
        [SugarColumn(ColumnName = "group_id", IsNullable = false)]
        public string GroupId { get; set; }
    
        [SugarColumn(ColumnName = "group_name", IsNullable = false)]
        public string GroupName { get; set; }
        
        [SugarColumn(ColumnName = "raw_data", IsNullable = false)]
        public string RawData { get; set; }
        
        [SugarColumn(ColumnName = "next_message_id", IsNullable = true)]
        public string NextMessageId { get; set; }
    
        [SugarColumn(ColumnName = "create_time", IsNullable = false)]
        public DateTime CreateTime { get; set; } = DateTime.Now;
    }
}