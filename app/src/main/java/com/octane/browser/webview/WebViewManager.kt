package com.octane.browser.webview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.webkit.WebViewCompat
import com.octane.BuildConfig
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.webview.bridge.BridgeManager
import timber.log.Timber

class WebViewManager(
    private val context: Context,
    private val bridgeManager: BridgeManager,
    private val featureManager: AdvancedFeatureManager
) {

    @SuppressLint("SetJavaScriptEnabled")
    fun createWebView(browserViewModel: BrowserViewModel): WebView {
        Timber.d("═══════════════════════════════════════")
        Timber.d("Creating WebView for complex DeFi apps")
        Timber.d("═══════════════════════════════════════")

        return WebView(context.applicationContext).apply {
            // ✅ CRITICAL: Enable hardware acceleration
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            // ✅ CRITICAL: High renderer priority for trading charts
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)
            }

            // Configure settings
            configureForComplexApps()

            // Enable advanced features
            featureManager.enableAllFeatures(this, settings)

            // Set clients
            webViewClient = CustomWebViewClient(context.applicationContext, browserViewModel, bridgeManager)
            webChromeClient = CustomWebChromeClient(browserViewModel)

            // Add JS bridge
            addJavascriptInterface(
                bridgeManager.createJavaScriptInterface(),
                "AndroidBridge"
            )

            // Setup cookie persistence
            setupCookies(this)

            // ✅ CRITICAL: Enable remote debugging
            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun WebView.configureForComplexApps() {
        settings.apply {
            // ═══ Core JavaScript ═══
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true

            // ═══ Storage ═══
            domStorageEnabled = true
            @Suppress("DEPRECATION")
            databaseEnabled = true

            // ═══ Caching ═══
            // LOAD_DEFAULT is safest for DeFi apps to ensure fresh prices
            cacheMode = WebSettings.LOAD_DEFAULT

            // ═══ Network ═══
            blockNetworkLoads = false
            blockNetworkImage = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            // ═══ Security ═══
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = true
            }

            allowContentAccess = true
            allowFileAccess = true
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = false
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = false

            // ═══ Viewport ═══
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false

            // ═══ Media ═══
            mediaPlaybackRequiresUserGesture = false

            // ═══ CRITICAL FIXES FOR BLACK SCREENS ═══

            // 1. Layout Algorithm: MUST be NORMAL for React/Next.js apps.
            // TEXT_AUTOSIZING causes 0px height bugs on "div#root"
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            // 2. Offscreen Pre-Raster: DISABLED.
            // This feature crashes WebGL contexts on many Android GPUs.
            offscreenPreRaster = false

            // 3. User Agent: Keep default to avoid anti-bot checks,
            // but append a tag if you need to identify your app.
            // userAgentString = "$userAgentString Octane/1.0"

            // ═══ Modern Features ═══
            setSupportMultipleWindows(true)
            setGeolocationEnabled(true)

            // Text encoding
            defaultTextEncodingName = "utf-8"

            // Disable saving form data (Security)
            @Suppress("DEPRECATION")
            saveFormData = false
        }
    }

    private fun setupCookies(webView: WebView) {
        try {
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            // ✅ CRITICAL: Enable third-party cookies (Required for RPCs, Auth, and iframes)
            cookieManager.setAcceptThirdPartyCookies(webView, true)
            cookieManager.flush()
        } catch (e: Exception) {
            Timber.e(e, "Failed to setup cookies")
        }
    }

    companion object {
        fun isWebViewAvailable(context: Context): Boolean {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WebViewCompat.getCurrentWebViewPackage(context) != null
                } else true
            } catch (_: Exception) { false }
        }
    }
}