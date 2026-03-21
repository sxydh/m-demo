using System.Collections.Generic;
using QuickStartDemo.Entities;

namespace QuickStartDemo.Repositories.Interfaces
{
    public interface IGroupRepository
    {
        bool AddGroups(List<GroupEntity> groups);
        bool UpdateGroup(GroupEntity group);
        List<GroupEntity> QueryGroups();
    }
}