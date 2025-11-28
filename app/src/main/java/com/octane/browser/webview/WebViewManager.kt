package com.octane.browser.webview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.View
import android.webkit.*
import com.octane.BuildConfig
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.webview.bridge.BridgeManager

class WebViewManager(
    private val context: Context,
    private val bridgeManager: BridgeManager
) {

    @SuppressLint("SetJavaScriptEnabled")
    fun createWebView(browserViewModel: BrowserViewModel): WebView {
        return WebView(context).apply {
            configureSettings()
            webViewClient = CustomWebViewClient(browserViewModel, bridgeManager)
            webChromeClient = CustomWebChromeClient(browserViewModel)

            // Add JavaScript interface for wallet bridge
            addJavascriptInterface(
                bridgeManager.createJavaScriptInterface(),
                "AndroidBridge"
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun WebView.configureSettings() {
        settings.apply {
            // JavaScript & Web Standards
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true

            // Storage APIs
            domStorageEnabled = true

            // ✅ REMOVED: databaseEnabled (deprecated in API 19, removed in API 33)
            // Modern web apps use IndexedDB/localStorage instead

            // Caching - Use modern approach
            cacheMode = WebSettings.LOAD_DEFAULT

            // ✅ REMOVED: setAppCacheEnabled/setAppCachePath (deprecated in API 33)
            // HTML5 AppCache is obsolete, replaced by Service Workers

            // Layout & Display
            useWideViewPort = true
            loadWithOverviewMode = true
            layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING

            // User Agent (appear as Chrome Mobile)
            userAgentString = buildModernUserAgent()

            // Zoom
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false

            // Media
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mediaPlaybackRequiresUserGesture = false
            }

            // Mixed Content (CRITICAL for Web3 apps)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // ⚠️ SECURITY: Only use for Web3 compatibility
                // Most dApps still use HTTP RPC endpoints
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }

            // File Access - Modern secure defaults
            allowFileAccess = false // ✅ Disabled for security

            // ✅ REMOVED: allowFileAccessFromFileURLs/allowUniversalAccessFromFileURLs
            // These are deprecated and default to false on modern Android

            // Hardware Acceleration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
            }

            // Fonts
            defaultFontSize = 16
            minimumFontSize = 8

            // Safe Browsing (phishing protection)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = true
            }

            // Debugging (disable in production)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
            }
        }
    }

    private fun buildModernUserAgent(): String {
        return "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.0.0 Mobile Safari/537.36 " +
                "Octane/1.0" // Custom identifier for analytics
    }
}