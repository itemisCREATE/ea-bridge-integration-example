using System.Windows.Forms;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;

namespace EABridge_Example_AddIn.UI
{
    public class Win32Window : IWin32Window
    {
        private readonly IntPtr handle;

        public Win32Window(IntPtr handle)
        {
            this.handle = handle;
        }

        public IntPtr Handle
        {
            get { return handle; }
        }

        public static IWin32Window GetMainWindowHandle()
        {
            Process process = Process.GetCurrentProcess();
            process.Refresh();
            IntPtr handle = process.MainWindowHandle;
            if (!IntPtr.Zero.Equals(handle))
            {
                return new Win32Window(handle);
            }
            else
            {
                return null;
            }
        }
    }
}