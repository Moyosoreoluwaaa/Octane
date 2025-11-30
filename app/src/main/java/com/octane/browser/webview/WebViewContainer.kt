package com.octane.browser.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.octane.browser.domain.managers.ThemeManager
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import timber.log.Timber
import kotlin.math.abs

/**
 * ✅ FIXED: WebView with proper dark mode support that respects ThemeManager
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    browserViewModel: BrowserViewModel,
    modifier: Modifier = Modifier,
    onScrollUp: () -> Unit,
    onScrollDown: () -> Unit
) {
    // ✅ INJECT ThemeManager to get current theme
    val themeManager: ThemeManager = koinInject()
    val currentTheme by themeManager.currentTheme.collectAsState()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var webView by remember { mutableStateOf<WebView?>(null) }

    // ✅ Calculate if we should use dark mode
    val isDarkMode = WebViewDarkModeManager.rememberIsDarkMode(currentTheme)

    // Track pending scroll restoration
    var pendingScrollX by remember { mutableStateOf<Int?>(null) }
    var pendingScrollY by remember { mutableStateOf<Int?>(null) }

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

    // ✅ REACT TO THEME CHANGES
    LaunchedEffect(currentTheme, isDarkMode) {
        webView?.let { wv ->
            Timber.d("🎨 Theme changed to: $currentTheme (isDark: $isDarkMode)")
            WebViewDarkModeManager.setupDarkMode(wv, currentTheme)

            // Set background color to prevent white flash
            wv.setBackgroundColor(if (isDarkMode) Color.BLACK else Color.WHITE)

            // Optional: reload to apply theme to current page
            // wv.reload()
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
        factory = { factoryContext ->
            WebView(factoryContext).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // ✅ Set initial background based on theme
                setBackgroundColor(if (isDarkMode) Color.BLACK else Color.WHITE)

                // Basic settings
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

                // ✅ APPLY DARK MODE
                WebViewDarkModeManager.setupDarkMode(this, currentTheme)

                // Setup callbacks
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

                setupScrollListener(onScrollUp, onScrollDown, browserViewModel)

                webView = this
            }
        },
        update = { wv ->
            // Update is called on recomposition
            // Don't recreate WebView, just update dark mode if needed
        }
    )
}

/**
 * Setup WebView callbacks
 */
private fun WebView.setupWebView(
    browserViewModel: BrowserViewModel,
    onScrollRestored: () -> Unit
) {
    webViewClient = object : android.webkit.WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            url?.let { browserViewModel.onPageStarted(it) }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            url?.let {
                browserViewModel.onPageFinished(it, view?.title ?: "")
                browserViewModel.onNavigationStateChanged(
                    canGoBack = view?.canGoBack() == true,
                    canGoForward = view?.canGoForward() == true
                )

                // Restore scroll after page loads
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

/**
 * Scroll detection with state tracking
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
        if (abs(delta) > scrollThreshold) {
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

        // Track scroll position (debounced)
        scrollTrackingJob?.cancel()
        scrollTrackingJob = kotlinx.coroutines.CoroutineScope(
            Dispatchers.Main
        ).launch {
            delay(500)
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