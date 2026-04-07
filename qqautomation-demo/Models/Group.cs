using System.Windows.Automation;

namespace qqautomation_demo.Models
{
    public class Group
    {
        public string Name { get; }
        public AutomationElement Element { get; }

        public Group(string name, AutomationElement element)
        {
            Name = name;
            Element = element;
        }
    }
}
