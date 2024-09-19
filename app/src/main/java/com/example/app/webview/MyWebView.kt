package com.example.app.webview

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun MyWebView(
    url: String,
    modifier: Modifier = Modifier,
    onWebViewCreated: (WebView) -> Unit = {}
) {
    // This WebView instance is part of Compose and must be remembered
    var webViewInstance: WebView? by remember { mutableStateOf(null) }

    // Track whether the WebView can go back
    var canGoBack by remember { mutableStateOf(false) }

    // Handle back presses within the WebView
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
                settings.javaScriptCanOpenWindowsAutomatically = true
                loadUrl(url)

                webViewInstance = this  // Assign WebView instance for tracking
                onWebViewCreated(this)

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
            // Only load the URL if it differs
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        }
    )
}