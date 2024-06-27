package cn.net.bhe.webviewdemo

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView = findViewById<WebView>(R.id.webView)
        /* 启用脚本 */
        webView.settings.javaScriptEnabled = true
        /* 禁用缓存 */
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        // 访问 http 地址需要显示配置 android:usesCleartextTraffic="true"
        webView.loadUrl("http://192.168.211.185:10006")
    }
}