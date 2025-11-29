package com.octane.browser.webview

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.os.bundleOf
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.webview.bridge.WalletBridge
import org.koin.compose.koinInject
import timber.log.Timber

@Composable
fun WebViewContainer(
    modifier: Modifier = Modifier,
    browserViewModel: BrowserViewModel = koinInject(),
    webViewManager: WebViewManager = koinInject(),
    walletBridge: WalletBridge = koinInject(),
    onScrollUp: () -> Unit = {},
    onScrollDown: () -> Unit = {},
    onWebViewCreated: ((WebView) -> Unit)? = null
) {
    val savedBundle: Bundle = rememberSaveable { bundleOf() }

    // Scroll threshold to prevent accidental triggering
    val scrollThreshold = 20

    val webView = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webViewManager.createWebView(browserViewModel).also { view ->
                walletBridge.setWebView(view)

                if (!savedBundle.isEmpty) {
                    view.restoreState(savedBundle)
                }

                // ✅ FIX: Native Scroll Listener
                // This detects scroll inside the WebView without stealing touches from Compose
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    view.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                        val diff = scrollY - oldScrollY
                        when {
                            diff > scrollThreshold -> onScrollDown() // Scrolling down, hide bars
                            diff < -scrollThreshold -> onScrollUp()  // Scrolling up, show bars
                        }
                    }
                }

                onWebViewCreated?.invoke(view)
            }
        } else {
            TODO("VERSION.SDK_INT < O")
        }
    }

    // Auto-capture screenshot
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        while (true) {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (webView.width > 0 && webView.height > 0) {
                        val bitmap = createBitmap(webView.width, webView.height)
                        val canvas = android.graphics.Canvas(bitmap)
                        webView.draw(canvas)
                        val thumbnail = bitmap.scale(400, (400 * bitmap.height) / bitmap.width.coerceAtLeast(1))
                        bitmap.recycle()
                        browserViewModel.captureCurrentTabScreenshot(thumbnail)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to capture screenshot")
            }
            kotlinx.coroutines.delay(30000)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        )
    }

    // Collect navigation events
    LaunchedEffect(Unit) {
        browserViewModel.navigationEvent.collect { event ->
            when (event) {
                is BrowserViewModel.NavigationEvent.LoadUrl -> {
                    if (webView.url != event.url) webView.loadUrl(event.url)
                }
                is BrowserViewModel.NavigationEvent.Reload -> webView.reload()
                is BrowserViewModel.NavigationEvent.GoBack -> if (webView.canGoBack()) webView.goBack()
                is BrowserViewModel.NavigationEvent.GoForward -> if (webView.canGoForward()) webView.goForward()
                is BrowserViewModel.NavigationEvent.StopLoading -> webView.stopLoading()
                is BrowserViewModel.NavigationEvent.SetDesktopMode -> {
                    webView.settings.apply {
                        userAgentString = if (event.enabled) {
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
                        } else null
                        useWideViewPort = event.enabled
                        loadWithOverviewMode = event.enabled
                    }
                    webView.reload()
                }
                else -> {}
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.saveState(savedBundle)
            webView.stopLoading()
            webView.destroy()
        }
    }
}