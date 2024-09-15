package com.example.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.app.ui.theme.AppTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Scaffold { paddingValues ->
                    var webView: WebView? by remember { mutableStateOf(null) }
                    var canGoBack by remember { mutableStateOf(false) }

                    MyWebView(
                        url = "https://digiedu.co.in/",
                        modifier = Modifier.padding(paddingValues),
                        onWebViewCreated = { createdWebView ->
                            webView = createdWebView
                            webView?.webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    // Update state for whether the WebView can go back
                                    canGoBack = view?.canGoBack() ?: false
                                }
                            }
                        }
                    )

                    // BackHandler to navigate back in WebView
                    BackHandler(enabled = canGoBack) {
                        webView?.goBack()
                    }
                }
            }
        }
    }
}

@Composable
fun MyWebView(
    url: String,
    modifier: Modifier = Modifier,
    onWebViewCreated: (WebView) -> Unit = {}
) {
    var canGoBack by remember { mutableStateOf(false) }
    var webViewInstance: WebView? = null

    BackHandler(enabled = canGoBack) {
        webViewInstance?.goBack()
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                loadUrl(url)

                onWebViewCreated(this)
                webViewInstance = this

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        canGoBack = canGoBack()
                    }
                }
            }
        },
        modifier = modifier,
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        }
    )
//    DisposableEffect(key1 = Unit) {
//        onDispose {
//            webViewInstance?.destroy()
//        }
//    }
}