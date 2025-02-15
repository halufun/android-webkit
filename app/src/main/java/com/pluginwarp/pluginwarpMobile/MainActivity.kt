package com.pluginwarp.pluginwarpMobile

import android.os.Bundle
import android.view.Window
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REMOTE_URL = "https://plugin-warp.github.io"
        private const val OFFLINE_FILE_NAME = "offline.html"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activitymain)

        val webView = findViewById<WebView>(R.id.webview)

        // Disable caching
        webView.settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
        webView.settings.domStorageEnabled = false
        webView.settings.javaScriptEnabled = true
        // Allow access to local files
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true

        // Copy offline.html from assets to internal storage if it doesn't exist yet
        val offlineFile = File(filesDir, OFFLINE_FILE_NAME)
        if (!offlineFile.exists()) {
            copyAssetToFile(OFFLINE_FILE_NAME, offlineFile)
        }

        // Set up the WebViewClient with error handling
        webView.webViewClient = object : WebViewClient() {
            // For API < 23
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                view.loadUrl("file://${offlineFile.absolutePath}")
            }

            // For API 23+
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                // Ensure we're handling the main frame's error
                if (request?.isForMainFrame == true) {
                    view?.loadUrl("file://${offlineFile.absolutePath}")
                }
            }

            // Optionally, catch HTTP errors (like 404) too
            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                if (request?.isForMainFrame == true) {
                    view?.loadUrl("file://${offlineFile.absolutePath}")
                }
            }
        }

        // Attempt to load the remote URL
        webView.loadUrl(REMOTE_URL)
    }

    // Helper function to copy a file from assets to the internal storage
    private fun copyAssetToFile(assetName: String, outFile: File) {
        assets.open(assetName).use { inputStream ->
            FileOutputStream(outFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}
