using System;
using System.ComponentModel.DataAnnotations.Schema;
using SqlSugar;

namespace QuickStartDemo.Entities
{
    [Table("t_message")]
    public class MessageEntity
    {
        [SugarColumn(IsPrimaryKey = true, IsIdentity = true)]
        public int Id { get; set; }
        
        [SugarColumn(ColumnName = "message_id", IsNullable = false)]
        public string MessageId { get; set; }
        
        [SugarColumn(ColumnName = "group_id", IsNullable = true)]
        public string GroupId { get; set; }
        
        [SugarColumn(ColumnName = "raw_message", IsNullable = true)]
        public string RawMessage { get; set; }
    
        [SugarColumn(ColumnName = "raw_data", IsNullable = false)]
        public string RawData { get; set; }
    
        [SugarColumn(ColumnName = "create_time", IsNullable = false)]
        public DateTime CreateTime { get; set; } = DateTime.Now;
    }
}