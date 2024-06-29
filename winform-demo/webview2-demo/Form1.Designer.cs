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

        private async void InitializeComponent()
        {
            var webView2 = new WebView2();
            (webView2 as System.ComponentModel.ISupportInitialize).BeginInit();
            webView2.Dock = System.Windows.Forms.DockStyle.Fill;
            webView2.Source = new System.Uri($"http://localhost:{fsPort}/", System.UriKind.Absolute);
            await webView2.EnsureCoreWebView2Async();
            webView2.CoreWebView2.Settings.AreDefaultContextMenusEnabled = false;
            this.Controls.Add(webView2);
            (webView2 as System.ComponentModel.ISupportInitialize).EndInit();

            this.components = new System.ComponentModel.Container();
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1800, 900);
            this.Text = "Form1";
        }

    }
}

