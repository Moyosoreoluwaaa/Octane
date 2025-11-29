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
            // ✅ CRITICAL FIX #1: Enable hardware acceleration
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            Timber.d("✅ Hardware acceleration: ENABLED")

            // ✅ CRITICAL FIX #2: High renderer priority
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)
                Timber.d("✅ Renderer priority: IMPORTANT")
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

            // ✅ CRITICAL FIX #4: Enable remote debugging
            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true)
                Timber.d("✅ Remote debugging: ENABLED")
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
            // ═══ Core JavaScript ═══
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true

            // ═══ Storage (IndexedDB + localStorage) ═══
            domStorageEnabled = true

            // ✅ FIXED: Use suppression for deprecated but necessary API
            @Suppress("DEPRECATION")
            databaseEnabled = true

            // ═══ Caching ═══
            cacheMode = WebSettings.LOAD_DEFAULT

            // Note: Application cache APIs removed in API 33+
            // PWAs now use Service Workers instead

            // ═══ Network ═══
            blockNetworkLoads = false
            blockNetworkImage = false

            // ✅ CRITICAL: Allow mixed content (HTTPS pages with HTTP resources)
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            // ═══ Security ═══
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = true
            }

            // ✅ CRITICAL FIX #5: File access for local assets
            allowContentAccess = true
            allowFileAccess = true

            // ✅ CRITICAL: Enable file access from file URLs (needed for some PWAs)
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = false // Security: Keep disabled for web content
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = false // Security: Keep disabled

            // ═══ Viewport ═══
            useWideViewPort = true
            loadWithOverviewMode = true

            // ═══ Zoom ═══
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false

            // ═══ Media ═══
            mediaPlaybackRequiresUserGesture = false

            // ✅ CRITICAL FIX #6: Layout algorithm for modern sites
            layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING

            // ✅ CRITICAL FIX #7: Keep default User-Agent (don't modify)
            // Custom UA causes CAPTCHA issues
            Timber.d("User Agent: ${userAgentString.take(80)}...")

            // ✅ CRITICAL FIX #8: Performance optimizations
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                offscreenPreRaster = true
            }

            // ✅ CRITICAL FIX #9: Support multiple windows (for popups, OAuth)
            setSupportMultipleWindows(true)

            // ✅ CRITICAL FIX #10: Geolocation (helps with some sites)
            setGeolocationEnabled(true)

            // ✅ CRITICAL FIX #11: Force Standards Mode rendering
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // This ensures the WebView always uses modern rendering
                layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
            }

            // ✅ CRITICAL FIX #12: Enable images automatically
            loadsImagesAutomatically = true

            // ✅ CRITICAL FIX #13: Enable all plugins
            @Suppress("DEPRECATION")
            pluginState = WebSettings.PluginState.ON_DEMAND

            // ✅ NEW: Text encoding
            defaultTextEncodingName = "utf-8"

            // ✅ NEW: Fix text size
            textZoom = 100

            // ✅ NEW: Enable loading resources (CSS, JS, images)
            loadsImagesAutomatically = true
            blockNetworkImage = false
            blockNetworkLoads = false

            // ✅ CRITICAL FIX #14: Disable save form data (privacy)
            @Suppress("DEPRECATION")
            saveFormData = false

            // ✅ CRITICAL FIX #15: Fix rendering on older devices
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }
    }

    private fun setupCookies(webView: WebView) {
        try {
            val cookieManager = CookieManager.getInstance()

            // Enable cookies
            cookieManager.setAcceptCookie(true)

            // ✅ CRITICAL: Enable third-party cookies (required for reCAPTCHA, OAuth, DApps)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(webView, true)
            }

            // Ensure cookies persist
            cookieManager.flush()

            Timber.d("✅ Cookies: Accept=${cookieManager.acceptCookie()}, ThirdParty=true")
        } catch (e: Exception) {
            Timber.e(e, "Failed to setup cookies")
        }
    }

    private fun WebView.logWebViewInfo() {
        Timber.d("─────────────────────────────────────")
        Timber.d("WebView Configuration Summary:")
        Timber.d("   JavaScript: ${settings.javaScriptEnabled}")
        Timber.d("   DOM Storage: ${settings.domStorageEnabled}")
        @Suppress("DEPRECATION")
        Timber.d("   Database: ${settings.databaseEnabled}")
        Timber.d("   Mixed Content: ${settings.mixedContentMode}")
        Timber.d("   Layout Algorithm: ${settings.layoutAlgorithm}")
        Timber.d("   Hardware Layer: ${layerType == View.LAYER_TYPE_HARDWARE}")
        Timber.d("   Offscreen Pre-Raster: ${settings.offscreenPreRaster}")
        Timber.d("   Images Auto: ${settings.loadsImagesAutomatically}")
        Timber.d("   Block Network: ${settings.blockNetworkLoads}")
        Timber.d("   Cache Mode: ${settings.cacheMode}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Timber.d("   Renderer Priority: IMPORTANT")
        }
        Timber.d("─────────────────────────────────────")
    }

    companion object {
        fun isWebViewAvailable(context: Context): Boolean {
            return try {
                val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WebViewCompat.getCurrentWebViewPackage(context)
                } else {
                    null
                }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WebViewCompat.getCurrentWebViewPackage(context)?.versionName
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}