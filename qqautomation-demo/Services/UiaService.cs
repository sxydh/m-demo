using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Automation;
using qqautomation_demo.Entities;

namespace qqautomation_demo.Services
{
    public class UiaService : IUiaService
    {
        private readonly DbService _db;

        public UiaService(DbService db)
        {
            _db = db;
        }

        public AutomationElement FindFirst(AutomationElement root, Condition condition,
            int maxDepth, bool isBfs = true)
        {
            var key = BuildKey(root, condition);
            var props = ExtractProperties(condition);
            var path = GetPath(key);
            if (path != null)
            {
                var cached = WalkByPath(root, path, condition, props);
                if (cached != null)
                    return cached;
                RemovePath(key);
            }

            var el = WalkAndFind(root, condition, props,
                maxDepth, out var foundPath, isBfs);
            if (el != null)
                SavePath(key, foundPath);

            return el;
        }
        
        public IDictionary<string, AutomationElement> GetElementCacheMap(
            AutomationElement root, int maxDepth, CacheRequest request)
        {
            var result = new Dictionary<string, AutomationElement>();

            if (root == null || maxDepth < 1 || request == null) return result;

            request.TreeScope = TreeScope.Element | TreeScope.Children;
            BuildElementCacheMapRecursive(root, "0", 1, maxDepth, request, result);

            return result;
        }

        private void BuildElementCacheMapRecursive(
            AutomationElement node,
            string path,
            int currentDepth,
            int maxDepth,
            CacheRequest request,
            IDictionary<string, AutomationElement> result)
        {
            AutomationElement updatedNode;
            try
            {
                updatedNode = node.GetUpdatedCache(request);
            }
            catch (ElementNotAvailableException)
            {
                return;
            }

            result[path] = updatedNode;

            if (currentDepth >= maxDepth)
            {
                return;
            }

            var children = updatedNode.CachedChildren;
            if (children == null) return;

            for (var i = 0; i < children.Count; i++)
            {
                var child = children[i];
                var childPath = path + "/" + i;
                BuildElementCacheMapRecursive(child, childPath, currentDepth + 1, maxDepth, request, result);
            }
        }

        public List<string> CollectTextsFromCacheAtDepth(
            IDictionary<string, AutomationElement> elementCacheMap,
            int depth)
        {
            return elementCacheMap
                .Where(kvp => kvp.Key.Count(c => c == '/') == depth - 1 &&
                              Equals(kvp.Value.Cached.ControlType, ControlType.Text))
                .Select(kvp =>
                {
                    try
                    {
                        return kvp.Value.Cached.Name;
                    }
                    catch (InvalidOperationException)
                    {
                        return null;
                    }
                })
                .Where(name => !string.IsNullOrEmpty(name))
                .ToList();
        }

        public List<AutomationElement> GetElementsFromCacheAtDepth(AutomationElement cachedRoot, int depth)
        {
            if (cachedRoot == null) throw new ArgumentNullException(nameof(cachedRoot), "The root element cannot be null");
            if (depth < 1) throw new ArgumentOutOfRangeException(nameof(depth), "Depth must be greater than or equal to 1");

            var currentLevelElements = new List<AutomationElement> { cachedRoot };
            for (var i = 1; i < depth; i++)
            {
                var nextLevelElements = new List<AutomationElement>();
                foreach (var element in currentLevelElements)
                {
                    var children = element.CachedChildren;
                    if (children != null && children.Count > 0)
                    {
                        foreach (AutomationElement child in children)
                        {
                            nextLevelElements.Add(child);
                        }
                    }
                }

                currentLevelElements = nextLevelElements;
                if (currentLevelElements.Count == 0) break;
            }

            return currentLevelElements;
        }

        private string BuildKey(AutomationElement root, Condition condition)
        {
            var rootName = root.Current.Name;
            var rootType = root.Current.ControlType.ProgrammaticName;
            return $"{rootType}:{rootName}|{SerializeCondition(condition)}";
        }

        private string SerializeCondition(Condition condition)
        {
            if (condition is PropertyCondition pc)
                return $"{pc.Property.ProgrammaticName}={pc.Value}";
            if (condition is AndCondition ac)
                return "AND(" + string.Join(",", Array.ConvertAll(ac.GetConditions(), SerializeCondition)) + ")";
            if (condition is OrCondition oc)
                return "OR(" + string.Join(",", Array.ConvertAll(oc.GetConditions(), SerializeCondition)) + ")";
            return condition.GetType().Name;
        }

        private IList<AutomationProperty> ExtractProperties(Condition condition)
        {
            var result = new List<AutomationProperty>();
            ExtractPropertiesRecursive(condition, result);
            return result;
        }

        private void ExtractPropertiesRecursive(Condition condition, List<AutomationProperty> result)
        {
            if (condition is PropertyCondition pc)
            {
                if (!result.Contains(pc.Property))
                    result.Add(pc.Property);
            }
            else if (condition is AndCondition ac)
            {
                foreach (var sub in ac.GetConditions())
                    ExtractPropertiesRecursive(sub, result);
            }
            else if (condition is OrCondition oc)
            {
                foreach (var sub in oc.GetConditions())
                    ExtractPropertiesRecursive(sub, result);
            }
        }

        private List<int> GetPath(string key)
        {
            var entity = _db.Db.Queryable<UiaPathCacheEntity>()
                .Where(e => e.Key == key)
                .First();
            return entity == null ? null : ParsePath(entity.Path);
        }

        private List<int> ParsePath(string pathStr)
        {
            var parts = pathStr.Split(',');
            var result = new List<int>();
            foreach (var p in parts)
            {
                if (!int.TryParse(p.Trim(), out var idx)) return null;
                result.Add(idx);
            }

            return result;
        }

        private AutomationElement WalkByPath(
            AutomationElement root, List<int> path,
            Condition condition, IList<AutomationProperty> props)
        {
            if (root == null) return null;

            var current = root;

            var request = new CacheRequest
            {
                TreeScope = TreeScope.Children,
                TreeFilter = Automation.ControlViewCondition
            };
            request.Add(AutomationElement.ControlTypeProperty);

            foreach (var index in path)
            {
                var updated = current.GetUpdatedCache(request);
                var children = updated.CachedChildren;

                if (children == null || index < 0 || index >= children.Count)
                    return null;

                current = children[index];
            }

            var finalRequest = BuildCacheRequest(props);
            var finalElement = current.GetUpdatedCache(finalRequest);

            return MatchesCondition(finalElement, condition) ? finalElement : null;
        }

        private AutomationElement WalkAndFind(
            AutomationElement root, Condition condition, IList<AutomationProperty> props,
            int maxDepth, out List<int> foundPath, bool isBfs = false)
        {
            if (isBfs)
            {
                var result = BfsWalkAndFind(root, condition, props, maxDepth, out var bfsFoundPath);
                foundPath = bfsFoundPath;
                return result;
            }

            var req = BuildCacheRequest(props);
            req.TreeScope = TreeScope.Element | TreeScope.Children;
            foundPath = new List<int>();
            return DfsWalkAndFind(root, condition, maxDepth, req, foundPath);
        }

        private AutomationElement BfsWalkAndFind(AutomationElement root, Condition condition,
            IList<AutomationProperty> props, int maxDepth, out List<int> foundPath)
        {
            var req = BuildCacheRequest(props);
            req.TreeScope = TreeScope.Element | TreeScope.Children;

            var cachedRoot = root.GetUpdatedCache(req);
            if (MatchesCondition(cachedRoot, condition))
            {
                foundPath = new List<int>();
                return cachedRoot;
            }

            var queue = new Queue<(AutomationElement element, int depth, List<int> path)>();
            queue.Enqueue((cachedRoot, 1, new List<int>()));
            while (queue.Count > 0)
            {
                var (current, depth, currentPath) = queue.Dequeue();
                if (depth >= maxDepth) continue;

                var children = current.CachedChildren;
                if (children == null || children.Count == 0) continue;
                for (var i = 0; i < children.Count; i++)
                {
                    var child = children[i];
                    var nextPath = new List<int>(currentPath) { i };

                    if (MatchesCondition(child, condition))
                    {
                        foundPath = nextPath;
                        return child;
                    }

                    if (depth + 1 < maxDepth)
                    {
                        try
                        {
                            var updatedChild = child.GetUpdatedCache(req);
                            queue.Enqueue((updatedChild, depth + 1, nextPath));
                        }
                        catch
                        {
                            // ignored
                        }
                    }
                }
            }

            foundPath = null;
            return null;
        }

        private AutomationElement DfsWalkAndFind(AutomationElement current,
            Condition condition, int maxDepth, CacheRequest req, List<int> path)
        {
            if (maxDepth <= 0) return null;

            AutomationElement cachedCurrent;
            try
            {
                cachedCurrent = current.GetUpdatedCache(req);
            }
            catch
            {
                return null;
            }

            if (MatchesCondition(cachedCurrent, condition))
            {
                return cachedCurrent;
            }

            if (maxDepth <= 1) return null;

            var children = cachedCurrent.CachedChildren;
            if (children == null || children.Count == 0) return null;

            for (var i = 0; i < children.Count; i++)
            {
                var child = children[i];
                path.Add(i);

                var found = DfsWalkAndFind(child, condition, maxDepth - 1, req, path);
                if (found != null) return found;

                path.RemoveAt(path.Count - 1);
            }

            return null;
        }

        private CacheRequest BuildCacheRequest(IList<AutomationProperty> props)
        {
            var req = new CacheRequest();
            foreach (var p in props)
                req.Add(p);
            return req;
        }

        private bool MatchesCondition(AutomationElement el, Condition condition)
        {
            if (condition is PropertyCondition pc)
                return Equals(el.GetCachedPropertyValue(pc.Property), pc.Value);
            if (condition is AndCondition ac)
            {
                foreach (var sub in ac.GetConditions())
                    if (!MatchesCondition(el, sub))
                        return false;
                return true;
            }

            if (condition is OrCondition oc)
            {
                foreach (var sub in oc.GetConditions())
                    if (MatchesCondition(el, sub))
                        return true;
            }

            return false;
        }

        private void SavePath(string key, List<int> path)
        {
            var pathStr = string.Join(",", path);
            var existing = _db.Db.Queryable<UiaPathCacheEntity>()
                .Where(e => e.Key == key)
                .First();
            if (existing == null)
                _db.Db.Insertable(new UiaPathCacheEntity { Key = key, Path = pathStr }).ExecuteCommand();
            else
                _db.Db.Updateable<UiaPathCacheEntity>()
                    .SetColumns(e => new UiaPathCacheEntity { Path = pathStr })
                    .Where(e => e.Key == key)
                    .ExecuteCommand();
        }

        private void RemovePath(string key)
        {
            _db.Db.Deleteable<UiaPathCacheEntity>()
                .Where(e => e.Key == key)
                .ExecuteCommand();
        }
    }
}