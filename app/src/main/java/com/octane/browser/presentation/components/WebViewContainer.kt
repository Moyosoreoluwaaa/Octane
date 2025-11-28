package com.octane.browser.presentation.components

import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.webview.WebViewManager
import com.octane.browser.webview.bridge.WalletBridge
import org.koin.compose.koinInject

@Composable
fun WebViewContainer(
    modifier: Modifier = Modifier,
    browserViewModel: BrowserViewModel = koinInject(),
    webViewManager: WebViewManager = koinInject(),
    walletBridge: WalletBridge = koinInject()
) {
    val navigationEvent by browserViewModel.navigationEvent.collectAsState(initial = null)
    
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                webViewManager.createWebView(browserViewModel).also { 
                    webView = it
                    walletBridge.setWebView(it)
                }
            },
            update = { view ->
                // Handle navigation events
                when (val event = navigationEvent) {
                    is BrowserViewModel.NavigationEvent.LoadUrl -> {
                        if (view.url != event.url) {
                            view.loadUrl(event.url)
                        }
                    }
                    is BrowserViewModel.NavigationEvent.Reload -> view.reload()
                    is BrowserViewModel.NavigationEvent.GoBack -> view.goBack()
                    is BrowserViewModel.NavigationEvent.GoForward -> view.goForward()
                    is BrowserViewModel.NavigationEvent.StopLoading -> view.stopLoading()
                    else -> {}
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
    
    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()
        }
    }
}