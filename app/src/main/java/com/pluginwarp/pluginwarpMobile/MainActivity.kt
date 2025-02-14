import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import com.pluginwarp.pluginwarpMobile.R

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activitymain)

        webView = findViewById(R.id.webView)

        // Enable JavaScript if needed
        webView.settings.javaScriptEnabled = true

        // Ensure links are opened within the WebView instead of an external browser
        webView.webViewClient = WebViewClient()

        // Load the specified website
        webView.loadUrl("https://Plugin-Warp.github.io")
    }

    // Handle back press to navigate within the WebView history
    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            return super.getOnBackInvokedDispatcher()
        }
        return super.getOnBackInvokedDispatcher()
    }

}