using SqlSugar;

namespace qqautomation_demo.Entities
{
    [SugarTable("t_uia_path_cache")]
    public class UiaPathCacheEntity
    {
        [SugarColumn(IsPrimaryKey = true, IsIdentity = true)]
        public int Id { get; set; }

        [SugarColumn(ColumnName = "key", Length = 1024, IsNullable = false)]
        public string Key { get; set; }

        [SugarColumn(ColumnName = "path", Length = 1024, IsNullable = false)]
        public string Path { get; set; }
    }
}