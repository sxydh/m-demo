using System.Collections.Generic;
using qqautomation_demo.Models;

namespace qqautomation_demo.Services
{
    public interface IQqAutoService
    {
        List<Group> GetGroupList();
        bool EnterGroup(Group group);
        List<string> ReadMessages();
        bool SendMessage(string text);
    }
}