package com.pluginwarp.pluginwarpMobile

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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

        // Inject JavaScript Interface
        webView.addJavascriptInterface(WebAppInterface(this), "Android")

        // Load the URL
        webView.loadUrl(REMOTE_URL)
    }

    // Function to download a blob (base64 encoded)
    @OptIn(ExperimentalEncodingApi::class)
    fun downloadBase64Blob(blobData: String, context: Context) {
        // Decode the base64 data to bytes
        val decodedBytes = Base64.decode(blobData, 0, blobData.length)

        // Save the decoded data to a file
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "downloaded_blob_file")
        try {
            val outputStream = FileOutputStream(file)
            outputStream.write(decodedBytes)
            outputStream.close()

            // Notify the user that the file has been downloaded
            Toast.makeText(context, "Blob download completed", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error downloading blob", Toast.LENGTH_SHORT).show()
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

    // JavaScript Interface class
    class WebAppInterface(private val context: Context) {

        // This method is called from JavaScript to send the blob data
        @JavascriptInterface
        fun onBlobReceived(blobData: String) {
            // Handle the base64 data received from the blob URL
            (context as MainActivity).downloadBase64Blob(blobData, context)
        }
    }
}
