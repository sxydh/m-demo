using System.Windows.Forms;

namespace webview2_demo
{
    public partial class Form1 : Form
    {

        private int fsPort;

        public Form1()
        {
            fsPort = Fs();
            InitializeComponent();
        }

        [System.Runtime.InteropServices.DllImport("fs.dll")]
        private static extern int Fs();
    }
}
