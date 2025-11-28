package com.octane.browser.webview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.RequiresApi
import com.octane.BuildConfig
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.webview.bridge.BridgeManager
import timber.log.Timber

@RequiresApi(Build.VERSION_CODES.O)
class WebViewManager(
    private val context: Context,
    private val bridgeManager: BridgeManager,
    private val featureManager: AdvancedFeatureManager
) {

    @SuppressLint("SetJavaScriptEnabled")
    fun createWebView(browserViewModel: BrowserViewModel): WebView {
        Timber.d("═══════════════════════════════════════")
        Timber.d("Creating WebView for complex DeFi apps (Drift.trade, etc.)")
        Timber.d("═══════════════════════════════════════")

        return WebView(context.applicationContext).apply {
            // Critical: High renderer priority for charts & live data
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)
            }

            // Configure settings for modern SPAs
            configureForComplexApps()

            // Enable all advanced features (WebGL, WebSockets, etc.)
            featureManager.enableAllFeatures(this, settings)

            // Use your FULL, powerful clients — NOT the old inner stub
            // ✅ UPDATED: Pass context.applicationContext to the CustomWebViewClient constructor
            webViewClient = CustomWebViewClient(context.applicationContext, browserViewModel, bridgeManager)
            webChromeClient = CustomWebChromeClient(browserViewModel)

            // Add JS bridge
            addJavascriptInterface(
                bridgeManager.createJavaScriptInterface(),
                "AndroidBridge"
            )

            // Enable remote debugging in debug builds
            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true)
            }

            if (BuildConfig.DEBUG) {
                featureManager.logAvailableFeatures()
                logWebViewInfo()
            }

            Timber.d("WebView fully configured and ready!")
            Timber.d("═══════════════════════════════════════")
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun WebView.configureForComplexApps() {
        settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            blockNetworkLoads = false
            blockNetworkImage = false

            // ✅ FIX 4: Change to COMPATIBILITY_MODE (safer/better for DApps)
            mixedContentMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            } else {
                WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            safeBrowsingEnabled = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                allowFileAccessFromFileURLs = false
                allowUniversalAccessFromFileURLs = false
            }

            allowContentAccess = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = false

            // ✅ FIX 3: Layout Algorithm MUST be NORMAL
            // This prevents React/TradingView charts from miscalculating their size and rendering blank.
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL

            // ✅ FIX 5: Custom User Agent (for better server compatibility)
            // Ensure you keep 'Mobile' in the string
            val defaultUA = userAgentString
            userAgentString = "$defaultUA OctaneBrowser/1.0"

            // Critical for smooth chart rendering
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                offscreenPreRaster = true
            }

            setSupportMultipleWindows(true)
        }
    }

    private fun WebView.logWebViewInfo() {
        Timber.d("─────────────────────────────────────")
        Timber.d("WebView Configuration Summary:")
        Timber.d("   JavaScript: ${settings.javaScriptEnabled}")
        Timber.d("   DOM Storage: ${settings.domStorageEnabled}")
        Timber.d("   Offscreen Pre-Raster: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) settings.offscreenPreRaster else "N/A"}")
        Timber.d("   Layout Algorithm: ${settings.layoutAlgorithm}")
        Timber.d("   Renderer Priority: IMPORTANT")
        Timber.d("─────────────────────────────────────")
    }

    companion object {
        fun isWebViewAvailable(context: Context): Boolean {
            return try {
                val packageInfo = WebView.getCurrentWebViewPackage()
                if (packageInfo != null) {
                    Timber.d("WebView available: ${packageInfo.versionName}")
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Timber.e(e, "WebView not available")
                false
            }
        }

        fun getWebViewVersion(context: Context): String? {
            return try {
                WebView.getCurrentWebViewPackage()?.versionName
            } catch (e: Exception) {
                null
            }
        }
    }
}