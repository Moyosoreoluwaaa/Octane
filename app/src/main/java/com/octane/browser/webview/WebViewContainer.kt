package com.octane.browser.webview

import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.webview.bridge.WalletBridge
import org.koin.compose.koinInject
import timber.log.Timber

/**
 * ✅ UPDATED: Now exposes WebView reference for diagnostics
 */
@Composable
fun WebViewContainer(
    modifier: Modifier = Modifier,
    browserViewModel: BrowserViewModel = koinInject(),
    webViewManager: WebViewManager = koinInject(),
    walletBridge: WalletBridge = koinInject(),
    onWebViewCreated: ((WebView) -> Unit)? = null // ✅ NEW callback
) {
    // ✅ Preserve WebView state across config changes (rotation, etc.)
    val savedBundle: Bundle = rememberSaveable { bundleOf() }

    // ✅ Remember WebView instance to avoid recreation
    val webView = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webViewManager.createWebView(browserViewModel).also { view ->
                walletBridge.setWebView(view)

                // Restore previous state if available
                if (!savedBundle.isEmpty) {
                    view.restoreState(savedBundle)
                    Timber.d("📦 Restored WebView state")
                }

                // ✅ NEW: Notify parent that WebView is created
                onWebViewCreated?.invoke(view)

                Timber.d("✅ WebView created")
            }
        } else {
            TODO("VERSION.SDK_INT < O")
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        )
    }

    // ✅ Collect navigation events in LaunchedEffect (proper SharedFlow handling)
    LaunchedEffect(Unit) {
        browserViewModel.navigationEvent.collect { event ->
            when (event) {
                is BrowserViewModel.NavigationEvent.LoadUrl -> {
                    Timber.d("🌐 Loading: ${event.url}")
                    if (webView.url != event.url) {
                        webView.loadUrl(event.url)
                    }
                }
                is BrowserViewModel.NavigationEvent.Reload -> {
                    Timber.d("🔄 Reloading")
                    webView.reload()
                }
                is BrowserViewModel.NavigationEvent.GoBack -> {
                    Timber.d("⬅️ Going back")
                    if (webView.canGoBack()) {
                        webView.goBack()
                    }
                }
                is BrowserViewModel.NavigationEvent.GoForward -> {
                    Timber.d("➡️ Going forward")
                    if (webView.canGoForward()) {
                        webView.goForward()
                    }
                }
                is BrowserViewModel.NavigationEvent.StopLoading -> {
                    Timber.d("⛔ Stopping")
                    webView.stopLoading()
                }
                else -> {}
            }
        }
    }

    // ✅ Save state on dispose and cleanup
    DisposableEffect(Unit) {
        onDispose {
            Timber.d("🧹 Disposing WebView")

            // Save state for restoration
            webView.saveState(savedBundle)

            // Cleanup
            webView.stopLoading()
            webView.destroy()
        }
    }
}