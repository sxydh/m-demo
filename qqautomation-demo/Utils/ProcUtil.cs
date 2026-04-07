using System;
using System.Diagnostics;
using System.Windows.Automation;

namespace qqautomation_demo.Utils
{
    public static class ProcUtil
    {
        public static AutomationElement FindWindowByName(string name)
        {
            var processes = Process.GetProcessesByName(name);
            if (processes.Length == 0)
                throw new InvalidOperationException($"The process '{name}' is not running.");

            foreach (var proc in processes)
            {
                if (proc.MainWindowHandle == IntPtr.Zero) continue;
                var element = AutomationElement.FromHandle(proc.MainWindowHandle);
                if (element != null) return element;
            }

            throw new InvalidOperationException($"'{name}' main window not found");
        }
    }
}