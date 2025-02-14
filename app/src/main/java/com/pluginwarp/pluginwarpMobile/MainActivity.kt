package com.pluginwarp.pluginwarpMobile

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activitymain)

        val webView = findViewById<WebView>(R.id.webview)
        webView.webViewClient = WebViewClient() // Ensures links open inside the WebView
        webView.settings.javaScriptEnabled = true // Enable JavaScript if needed
        webView.loadUrl("https://plugin-warp.github.io") // Replace with your URL
    }
}
