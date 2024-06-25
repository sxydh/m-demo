using Microsoft.Web.WebView2.WinForms;

namespace webview2_demo
{
    partial class Form1
    {
        private System.ComponentModel.IContainer components = null;

        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        private void InitializeComponent()
        {
            var webView2 = new WebView2
            {
                Dock = System.Windows.Forms.DockStyle.Fill,
                Source = new System.Uri("https://www.baidu.com")
            };

            this.components = new System.ComponentModel.Container();
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1800, 900);
            this.Text = "Form1";
            this.Controls.Add(webView2);
        }

    }
}

