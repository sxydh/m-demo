using System.Collections.Generic;
using System.Windows.Automation;

namespace qqautomation_demo.Services
{
    public interface IUiaService
    {
        AutomationElement FindFirst(
            AutomationElement root, 
            Condition condition, 
            int maxDepth, 
            bool isBfs = false);
        IDictionary<string, AutomationElement> GetElementCacheMap(
            AutomationElement root, 
            int maxDepth, 
            CacheRequest request);
        List<string> CollectTextsFromCacheAtDepth(
            IDictionary<string, AutomationElement> elementCacheMap,
            int depth);
        List<AutomationElement> GetElementsFromCacheAtDepth(
            AutomationElement cachedRoot, 
            int depth);
    }
}