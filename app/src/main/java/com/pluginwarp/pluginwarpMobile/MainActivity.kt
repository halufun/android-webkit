package com.pluginwarp.pluginwarpMobile

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import android.webkit.WebChromeClient
import android.webkit.ConsoleMessage
import java.util.Locale

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
        webView.settings.apply {
            cacheMode = WebSettings.LOAD_NO_CACHE
            domStorageEnabled = false
            javaScriptEnabled = true
            allowFileAccess = true
            allowContentAccess = true
        }

        // Copy offline.html from assets to internal storage if it doesn't exist yet
        val offlineFile = File(filesDir, OFFLINE_FILE_NAME)
        if (!offlineFile.exists()) {
            copyAssetToFile(OFFLINE_FILE_NAME, offlineFile)
        }

        // Set WebView client
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()

                // Check if the link is from a different domain
                if (!url.contains("your-original-domain.com")) {
                    // Open the URL in the browser
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    return true
                }

                return super.shouldOverrideUrlLoading(view, request)
            }
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                view.loadUrl("file://${offlineFile.absolutePath}")
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (request?.isForMainFrame == true) {
                    view?.loadUrl("file://${offlineFile.absolutePath}")
                }
            }

            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                if (request?.isForMainFrame == true) {
                    view?.loadUrl("file://${offlineFile.absolutePath}")
                }
            }
        }
        var messageName = "extension"
        // Set up WebChromeClient to capture console messages
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                // Capture and log the console messages from JavaScript
                if (message != null) {
                    val sourceId = message.sourceId()
                    val messageText = message.message()

                    if (sourceId.contains("ExtensionDownloader.js") && messageText.startsWith("https:")) {
                        messageName = messageText.substringAfterLast("/")
                    }

                    // Filter out anything that isn't from ExtensionDownloader.js
                    if (sourceId.contains("ExtensionDownloader.js") && !messageText.startsWith("https:")) {
                        // Download the message as a .js file
                        downloadStringAs(this@MainActivity, messageName, messageText)
                    }
                }
                return super.onConsoleMessage(message)
            }
        }



        // Load the URL
        webView.loadUrl(REMOTE_URL)
    }





    // Function to download the string as a .js file (Android 7+)
    fun downloadStringAs(context: Context, name: String, content: String) {
        // Create a file name (for example: console_message.txt)
        val fileName = name
        val filetype = "js"

        // Get the public Downloads directory
        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsFolder.exists()) {
            downloadsFolder.mkdirs()
        }

        // Create the file and write the content to it
        val file = File(downloadsFolder, fileName)
        try {
            file.writeText(content)

            // Optionally, notify the media scanner about the new file so that it shows up in Downloads apps immediately
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)

            // Determine a basic MIME type based on the file extension.
            val mimeType = when (filetype.lowercase(Locale.getDefault())) {
                "txt"  -> "text/plain"
                "html" -> "text/html"
                "json" -> "application/json"
                else   -> "*/*"
            }

            // Add the file as a completed download to the system DownloadManager
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.addCompletedDownload(
                fileName,                   // Title
                "Downloaded using the app", // Description
                true,                       // Scannable by media scanner
                mimeType,                   // MIME type
                file.absolutePath,          // File path
                file.length(),              // File size in bytes
                true                        // Show in the system's Downloads UI
            )

            Toast.makeText(context, "File downloaded: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Copy offline.html from assets to internal storage
    private fun copyAssetToFile(assetName: String, outFile: File) {
        assets.open(assetName).use { inputStream ->
            FileOutputStream(outFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }


}
