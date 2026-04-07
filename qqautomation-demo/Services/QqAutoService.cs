using System;
using System.Collections.Generic;
using System.Windows.Automation;
using qqautomation_demo.Models;
using qqautomation_demo.Utils;

namespace qqautomation_demo.Services
{
    public class QqAutoService : IQqAutoService
    {
        private readonly UiaService _uiaService;
        private readonly AutomationElement _qqWindow;

        public QqAutoService(UiaService uiaService)
        {
            _uiaService = uiaService;
            _qqWindow = ProcUtil.FindWindowByName("QQ");
        }

        public List<Group> GetGroupList()
        {
            var condition = new PropertyCondition(AutomationElement.NameProperty, "会话列表");
            var sessionListNode = _uiaService.FindFirst(_qqWindow, condition, 9, isBfs: false);
            if (sessionListNode == null)
                throw new InvalidOperationException("Session list control not found");

            var req = new CacheRequest
            {
                TreeScope = TreeScope.Children,
            };
            req.Add(AutomationElement.NameProperty);
            req.Add(AutomationElement.ControlTypeProperty);

            var cachedSessionNode = sessionListNode.GetUpdatedCache(req);
            var children = cachedSessionNode.CachedChildren;

            var groupList = new List<Group>(children.Count);
            foreach (AutomationElement cachedChild in children)
            {
                var elementCacheMap = _uiaService.GetElementCacheMap(cachedChild, 4, req);
                var textList = _uiaService.CollectTextsFromCacheAtDepth(elementCacheMap, 4);
                if (textList.Count > 0)
                    groupList.Add(new Group(string.Concat(textList), cachedChild));
            }

            return groupList;
        }

        public bool EnterGroup(Group group) => throw new NotImplementedException();

        public List<string> ReadMessages()
        {
            // UIA 是条死路
            throw new NotImplementedException();
        }

        public bool SendMessage(string text) => throw new NotImplementedException();
    }
}