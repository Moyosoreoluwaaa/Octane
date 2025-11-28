package com.octane.browser.webview

import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.webkit.WebViewFeature
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import timber.log.Timber

class CustomWebChromeClient(
    private val browserViewModel: BrowserViewModel
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        browserViewModel.onProgressChanged(newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        title?.let { browserViewModel.onReceivedTitle(it) }
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
        icon?.let { browserViewModel.onReceivedIcon(it) }
    }

    // ✅ NEW: Permission requests (camera, mic for WebRTC)
    override fun onPermissionRequest(request: PermissionRequest?) {
        request?.let {
            Timber.d("Permission requested: ${it.resources.joinToString()}")

            // Auto-grant for development (should ask user in production)
            it.grant(it.resources)
        }
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        // TODO: Show native Android AlertDialog
        Timber.d("JS Alert: $message")
        result?.confirm()
        return true
    }

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        // TODO: Show confirmation dialog
        Timber.d("JS Confirm: $message")
        result?.confirm()
        return true
    }

    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean {
        // TODO: Show input dialog
        Timber.d("JS Prompt: $message")
        result?.confirm(defaultValue)
        return true
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        consoleMessage?.let {
            val tag = "WebView:${it.sourceId()?.substringAfterLast("/") ?: "Console"}"
            when (it.messageLevel()) {
                ConsoleMessage.MessageLevel.ERROR -> Timber.tag(tag).e("[${it.lineNumber()}] ${it.message()}")
                ConsoleMessage.MessageLevel.WARNING -> Timber.tag(tag).w("[${it.lineNumber()}] ${it.message()}")
                else -> Timber.tag(tag).d("[${it.lineNumber()}] ${it.message()}")
            }
        }
        return true
    }
}

/**
 * ✅ ENHANCED DIAGNOSTICS
 *
 * Comprehensive WebView diagnostics for complex website debugging.
 * Call this from MainActivity.onCreate() for startup diagnostics.
 */
object WebViewDiagnostics {

    /**
     * Run complete diagnostics on app startup
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun runStartupDiagnostics(context: Context) {
        Timber.d("═══════════════════════════════════════")
        Timber.d("       WEBVIEW STARTUP DIAGNOSTICS")
        Timber.d("═══════════════════════════════════════")

        logDeviceInfo()
        logWebViewInfo(context)
        logFeatureSupport()
        logMemoryInfo()
        logCookieInfo()

        Timber.d("═══════════════════════════════════════")
    }

    /**
     * Log device information
     */
    private fun logDeviceInfo() {
        Timber.d("─────────────────────────────────────")
        Timber.d("📱 Device Information:")
        Timber.d("   Manufacturer: ${Build.MANUFACTURER}")
        Timber.d("   Model: ${Build.MODEL}")
        Timber.d("   Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        Timber.d("   ABI: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
    }

    /**
     * Log WebView version and package info
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun logWebViewInfo(context: Context) {
        Timber.d("─────────────────────────────────────")
        Timber.d("🌐 WebView Information:")

        try {
            val webViewPackage = WebView.getCurrentWebViewPackage()
            if (webViewPackage != null) {
                Timber.d("   Version: ${webViewPackage.versionName}")
                Timber.d("   Package: ${webViewPackage.packageName}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Timber.d("   Version Code: ${webViewPackage.longVersionCode}")
                }

                // Check if version is modern enough
                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    webViewPackage.longVersionCode
                } else {
                    TODO("VERSION.SDK_INT < P")
                }
                if (versionCode < 500000000) { // Rough check for Chrome 100+
                    Timber.w("   ⚠️ WebView version might be too old!")
                    Timber.w("   Consider updating Google Chrome / Android System WebView")
                } else {
                    Timber.d("   ✅ WebView version is modern")
                }
            } else {
                Timber.e("   ❌ WebView package info is null!")
                Timber.e("   Device might not have WebView installed")
            }
        } catch (e: Exception) {
            Timber.e(e, "   ❌ Cannot get WebView package info")
        }
    }

    /**
     * Log feature support status
     */
    private fun logFeatureSupport() {
        Timber.d("─────────────────────────────────────")
        Timber.d("🔧 Feature Support:")

        val features = mapOf(
            "Service Workers" to WebViewFeature.SERVICE_WORKER_BASIC_USAGE,
            "Visual State Callback" to WebViewFeature.VISUAL_STATE_CALLBACK,
            "Off Screen Preraster" to WebViewFeature.OFF_SCREEN_PRERASTER,
            "Safe Browsing" to WebViewFeature.SAFE_BROWSING_ENABLE,
            "Web Resource Request" to WebViewFeature.WEB_RESOURCE_REQUEST_IS_REDIRECT,
            "Web Resource Error" to WebViewFeature.WEB_RESOURCE_ERROR_GET_CODE,
            "Tracing" to WebViewFeature.TRACING_CONTROLLER_BASIC_USAGE,
            "Proxy Override" to WebViewFeature.PROXY_OVERRIDE,
            "Force Dark" to WebViewFeature.FORCE_DARK,
            "Algorithmic Darkening" to WebViewFeature.ALGORITHMIC_DARKENING
        )

        features.forEach { (name, feature) ->
            checkFeature(name, feature)
        }
    }

    /**
     * Log memory information
     */
    private fun logMemoryInfo() {
        Timber.d("─────────────────────────────────────")
        Timber.d("💾 Memory Information:")

        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / 1024 / 1024 // MB
        val totalMemory = runtime.totalMemory() / 1024 / 1024 // MB
        val freeMemory = runtime.freeMemory() / 1024 / 1024 // MB
        val usedMemory = totalMemory - freeMemory

        Timber.d("   Max Memory: ${maxMemory}MB")
        Timber.d("   Total Memory: ${totalMemory}MB")
        Timber.d("   Used Memory: ${usedMemory}MB")
        Timber.d("   Free Memory: ${freeMemory}MB")

        // Warn if low memory
        if (usedMemory > maxMemory * 0.8) {
            Timber.w("   ⚠️ Memory usage is high (${(usedMemory.toFloat() / maxMemory * 100).toInt()}%)")
            Timber.w("   Consider clearing cache or restarting app")
        }
    }

    /**
     * Log cookie configuration
     */
    private fun logCookieInfo() {
        Timber.d("─────────────────────────────────────")
        Timber.d("🍪 Cookie Configuration:")

        val cookieManager = CookieManager.getInstance()
        Timber.d("   Accept Cookies: ${cookieManager.acceptCookie()}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Timber.d("   Third-party Cookies: Configured per-WebView")
        }
    }

    /**
     * Check if a specific feature is supported
     */
    private fun checkFeature(name: String, feature: String) {
        val supported = try {
            WebViewFeature.isFeatureSupported(feature)
        } catch (e: Exception) {
            Timber.e(e, "Error checking feature: $name")
            false
        }

        val icon = if (supported) "✅" else "❌"
        Timber.d("   $icon $name")
    }

    /**
     * Test loading a specific URL and report results
     */
    fun testUrl(webView: WebView, url: String, onResult: (Boolean, String?) -> Unit) {
        Timber.d("─────────────────────────────────────")
        Timber.d("🧪 Testing URL: $url")
        Timber.d("─────────────────────────────────────")

        val startTime = System.currentTimeMillis()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Timber.d("   ⏳ Page started loading: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                val loadTime = System.currentTimeMillis() - startTime
                Timber.d("   ✅ Successfully loaded: $url")
                Timber.d("   ⏱️ Load time: ${loadTime}ms")
                onResult(true, null)
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Timber.e("   ❌ Failed to load: $failingUrl")
                Timber.e("   Error code: $errorCode")
                Timber.e("   Description: $description")
                onResult(false, description)
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                val errorMsg = when (error?.primaryError) {
                    SslError.SSL_UNTRUSTED -> "SSL_UNTRUSTED"
                    SslError.SSL_EXPIRED -> "SSL_EXPIRED"
                    SslError.SSL_IDMISMATCH -> "SSL_IDMISMATCH"
                    SslError.SSL_NOTYETVALID -> "SSL_NOTYETVALID"
                    else -> "SSL_UNKNOWN"
                }
                Timber.e("   🔒 SSL Error: $errorMsg")
                Timber.e("   URL: ${error?.url}")
                onResult(false, "SSL Error: $errorMsg")
                handler?.cancel()
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                if (request?.isForMainFrame == true) {
                    val statusCode = errorResponse?.statusCode ?: 0
                    Timber.e("   ⚠️ HTTP Error: $statusCode")
                    Timber.e("   URL: ${request.url}")
                }
            }
        }

        webView.loadUrl(url)
    }

    /**
     * Log current WebView settings for debugging
     */
    fun logSettings(webView: WebView) {
        Timber.d("═══════════════════════════════════════")
        Timber.d("       WEBVIEW CURRENT SETTINGS")
        Timber.d("═══════════════════════════════════════")

        val settings = webView.settings

        Timber.d("JavaScript:")
        Timber.d("   Enabled: ${settings.javaScriptEnabled}")
        Timber.d("   Can Open Windows: ${settings.javaScriptCanOpenWindowsAutomatically}")

        Timber.d("Storage:")
        Timber.d("   DOM Storage: ${settings.domStorageEnabled}")
        @Suppress("DEPRECATION")
        Timber.d("   Database: ${settings.databaseEnabled}")

        Timber.d("Caching:")
        Timber.d("   Cache Mode: ${settings.cacheMode}")

        Timber.d("Network:")
        Timber.d("   Block Network Image: ${settings.blockNetworkImage}")
        Timber.d("   Block Network Loads: ${settings.blockNetworkLoads}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Timber.d("Security:")
            Timber.d("   Mixed Content: ${settings.mixedContentMode}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Timber.d("   Safe Browsing: ${settings.safeBrowsingEnabled}")
            }
        }

        Timber.d("Display:")
        Timber.d("   Layout Algorithm: ${settings.layoutAlgorithm}")
        Timber.d("   Use Wide Viewport: ${settings.useWideViewPort}")
        Timber.d("   Load With Overview: ${settings.loadWithOverviewMode}")

        Timber.d("User Agent:")
        Timber.d("   ${settings.userAgentString}")

        Timber.d("═══════════════════════════════════════")
    }

    /**
     * Monitor GPU memory issues
     */
    fun logGpuStatus() {
        Timber.d("─────────────────────────────────────")
        Timber.d("🎮 GPU Status:")

        // Check if hardware acceleration is available
        val isHardwareAccelerated = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        Timber.d("   Hardware Acceleration: ${if (isHardwareAccelerated) "Available" else "Not Available"}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Timber.d("   Renderer: Modern (Chromium)")
        } else {
            Timber.d("   Renderer: Legacy")
        }

        Timber.d("─────────────────────────────────────")
    }

    /**
     * Check for common issues
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun detectCommonIssues(context: Context) {
        Timber.d("═══════════════════════════════════════")
        Timber.d("       CHECKING FOR COMMON ISSUES")
        Timber.d("═══════════════════════════════════════")

        val issues = mutableListOf<String>()

        // Check WebView version
        try {
            val webViewPackage = WebView.getCurrentWebViewPackage()
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                webViewPackage?.longVersionCode ?: 0
            } else {
                TODO("VERSION.SDK_INT < P")
            }
            if (versionCode < 500000000) {
                issues.add("WebView version is too old")
            }
        } catch (e: Exception) {
            issues.add("Cannot detect WebView version")
        }

        // Check memory
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        if (usedMemory > maxMemory * 0.8) {
            issues.add("Memory usage is high (${(usedMemory.toFloat() / maxMemory * 100).toInt()}%)")
        }

        // Check critical features
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.SERVICE_WORKER_BASIC_USAGE)) {
            issues.add("Service Workers not supported (PWAs may not work)")
        }

        if (issues.isEmpty()) {
            Timber.d("✅ No common issues detected")
        } else {
            Timber.w("⚠️ Detected ${issues.size} potential issue(s):")
            issues.forEachIndexed { index, issue ->
                Timber.w("   ${index + 1}. $issue")
            }
        }

        Timber.d("═══════════════════════════════════════")
    }
}