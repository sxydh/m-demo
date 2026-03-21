using System.Collections.Generic;
using QuickStartDemo.Entities;
using QuickStartDemo.Helpers;
using QuickStartDemo.Repositories.Interfaces;

namespace QuickStartDemo.Repositories.Implementations
{
    public class GroupRepository : IGroupRepository
    {

        private readonly DbContext _dbContext;
        
        public GroupRepository(DbContext dbContext)
        {
            _dbContext = dbContext;
        }
        
        public bool AddGroups(List<GroupEntity> groups)
        {
            var storage = _dbContext.Db.Storageable(groups)
                .WhereColumns(it => it.GroupId)
                .ToStorage();
            return storage.AsInsertable.ExecuteCommand() > 0;
        }

        public bool UpdateGroup(GroupEntity group)
        {
            return _dbContext.Db.Updateable(group).ExecuteCommand() > 0;
        }

        public List<GroupEntity> QueryGroups()
        {
            return _dbContext.Db.Queryable<GroupEntity>().ToList();
        }
    }
}