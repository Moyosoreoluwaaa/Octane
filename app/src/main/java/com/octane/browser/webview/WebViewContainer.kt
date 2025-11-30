package com.octane.browser.webview

import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.abs

/**
 * ✅ ENHANCED: Proper scroll state tracking + Dark Theme Support
 *
 * Key Features:
 * 1. Real-time scroll position tracking
 * 2. Debounced saving to avoid database spam
 * 3. Automatic dark theme detection and application
 * 4. Configuration change handling
 * 5. Screenshot capture on lifecycle pause
 */
@Composable
fun WebViewContainer(
    browserViewModel: BrowserViewModel,
    modifier: Modifier = Modifier,
    onScrollUp: () -> Unit,
    onScrollDown: () -> Unit
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current // ✅ NEW: Get context for theme detection
    var webView by remember { mutableStateOf<WebView?>(null) }

    // ✅ Track pending scroll restoration
    var pendingScrollX by remember { mutableStateOf<Int?>(null) }
    var pendingScrollY by remember { mutableStateOf<Int?>(null) }

    // ✅ NEW: Track system theme changes (proper Compose way)
    val isDarkMode = androidx.compose.foundation.isSystemInDarkTheme()

    // Screenshot capture on lifecycle pause
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    webView?.let { wv ->
                        coroutineScope.launch {
                            captureWebViewScreenshot(wv)?.let { screenshot ->
                                browserViewModel.captureCurrentTabScreenshot(screenshot)
                            }
                        }
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // ✅ NEW: Update theme when system theme changes
    LaunchedEffect(isDarkMode) {
        webView?.let { wv ->
            WebViewThemeManager.setupTheme(wv, context)
            Timber.d("🎨 Theme updated: Dark Mode = $isDarkMode")
        }
    }

    // Navigation events
    LaunchedEffect(Unit) {
        browserViewModel.navigationEvent.collect { event ->
            webView?.let { wv ->
                when (event) {
                    is BrowserViewModel.NavigationEvent.LoadUrl -> {
                        Timber.d("🌐 Loading URL: ${event.url}")
                        wv.loadUrl(event.url)
                    }

                    is BrowserViewModel.NavigationEvent.RestoreScroll -> {
                        Timber.d("📜 Restoring scroll: (${event.x}, ${event.y})")
                        pendingScrollX = event.x
                        pendingScrollY = event.y
                    }

                    is BrowserViewModel.NavigationEvent.Reload -> {
                        wv.reload()
                    }
                    is BrowserViewModel.NavigationEvent.GoBack -> {
                        if (wv.canGoBack()) wv.goBack()
                    }
                    is BrowserViewModel.NavigationEvent.GoForward -> {
                        if (wv.canGoForward()) wv.goForward()
                    }
                    is BrowserViewModel.NavigationEvent.StopLoading -> {
                        wv.stopLoading()
                    }
                    is BrowserViewModel.NavigationEvent.SetDesktopMode -> {
                        wv.settings.apply {
                            userAgentString = if (event.enabled) {
                                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
                                        "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                            } else {
                                null
                            }
                        }
                        wv.reload()
                    }
                    else -> {}
                }
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // ✅ Setup WebView with callbacks
                setupWebView(
                    browserViewModel = browserViewModel,
                    onScrollRestored = {
                        if (pendingScrollX != null && pendingScrollY != null) {
                            scrollTo(pendingScrollX!!, pendingScrollY!!)
                            Timber.d("✅ Scroll restored: ($pendingScrollX, $pendingScrollY)")
                            pendingScrollX = null
                            pendingScrollY = null
                        }
                    }
                )

                // ✅ NEW: Setup dark theme (initial)
                WebViewThemeManager.setupTheme(this, context)
                WebViewThemeManager.logThemeConfiguration(this, context)

                // ✅ Setup scroll listener
                setupScrollListener(onScrollUp, onScrollDown, browserViewModel)

                webView = this
            }
        },
        update = { wv ->
            // ✅ NEW: Update theme on recomposition
            WebViewThemeManager.setupTheme(wv, context)
        }
    )
}

/**
 * ✅ Scroll detection with state tracking
 */
private fun WebView.setupScrollListener(
    onScrollUp: () -> Unit,
    onScrollDown: () -> Unit,
    browserViewModel: BrowserViewModel
) {
    var lastScrollY = 0
    var isScrollingDown = false
    var scrollTrackingJob: Job? = null

    val scrollThreshold = 10

    viewTreeObserver.addOnScrollChangedListener {
        val currentScrollY = scrollY
        val delta = currentScrollY - lastScrollY

        // UI bar visibility
        if (Math.abs(delta) > scrollThreshold) {
            when {
                delta > 0 -> {
                    if (!isScrollingDown) {
                        isScrollingDown = true
                        onScrollDown()
                    }
                }
                delta < 0 -> {
                    if (isScrollingDown) {
                        isScrollingDown = false
                        onScrollUp()
                    }
                }
            }
        }

        // ✅ Track scroll position (debounced)
        scrollTrackingJob?.cancel()
        scrollTrackingJob = kotlinx.coroutines.CoroutineScope(
            kotlinx.coroutines.Dispatchers.Main
        ).launch {
            delay(500) // Wait for scroll to stop
            browserViewModel.onScrollChanged(scrollX, currentScrollY)
        }

        lastScrollY = currentScrollY
    }
}

/**
 * Captures WebView screenshot
 */
private suspend fun captureWebViewScreenshot(webView: WebView): Bitmap? =
    withContext(Dispatchers.Main) {
        try {
            val width = webView.width
            val height = webView.height

            if (width <= 0 || height <= 0) {
                Timber.w("Invalid WebView dimensions: ${width}x$height")
                return@withContext null
            }

            val bitmap = createBitmap(width, height)
            val canvas = android.graphics.Canvas(bitmap)
            webView.draw(canvas)

            val thumbnailWidth = 400
            val thumbnailHeight = (height.toFloat() / width * thumbnailWidth).toInt()
            val thumbnail = bitmap.scale(thumbnailWidth, thumbnailHeight)

            bitmap.recycle()
            Timber.d("📸 Screenshot: ${thumbnail.width}x${thumbnail.height}")

            thumbnail

        } catch (e: Exception) {
            Timber.e(e, "Screenshot capture failed")
            null
        }
    }

/**
 * Setup WebView callbacks
 */
private fun WebView.setupWebView(
    browserViewModel: BrowserViewModel,
    onScrollRestored: () -> Unit
) {
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        loadWithOverviewMode = true
        useWideViewPort = true
        builtInZoomControls = true
        displayZoomControls = false
        setSupportZoom(true)
        setSupportMultipleWindows(false)
        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
    }

    webViewClient = object : android.webkit.WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            url?.let { browserViewModel.onPageStarted(it) }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            url?.let {
                browserViewModel.onPageFinished(it, view?.title ?: "")
                browserViewModel.onNavigationStateChanged(
                    canGoBack = view?.canGoBack() ?: false,
                    canGoForward = view?.canGoForward() ?: false
                )

                // ✅ Restore scroll after page loads
                view?.post {
                    onScrollRestored()
                }
            }
        }
    }

    webChromeClient = object : android.webkit.WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            browserViewModel.onProgressChanged(newProgress)
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            title?.let { browserViewModel.onReceivedTitle(it) }
        }

        override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
            icon?.let { browserViewModel.onReceivedIcon(it) }
        }
    }
}