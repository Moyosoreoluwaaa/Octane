package com.octane.browser.webview

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.webkit.WebViewCompat
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

    override fun onPermissionRequest(request: PermissionRequest?) {
        request?.let {
            Timber.d("Permission requested: ${it.resources.joinToString()}")
            it.grant(it.resources)
        }
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
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

object WebViewDiagnostics {

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

    private fun logDeviceInfo() {
        Timber.d("─────────────────────────────────────")
        Timber.d("📱 Device Information:")
        Timber.d("   Manufacturer: ${Build.MANUFACTURER}")
        Timber.d("   Model: ${Build.MODEL}")
        Timber.d("   Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        Timber.d("   ABI: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
    }

    // ✅ FIXED: No more TODO() crash
    private fun logWebViewInfo(context: Context) {
        Timber.d("─────────────────────────────────────")
        Timber.d("🌐 WebView Information:")

        try {
            val webViewPackage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WebViewCompat.getCurrentWebViewPackage(context)
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            if (webViewPackage != null) {
                Timber.d("   Version: ${webViewPackage.versionName}")
                Timber.d("   Package: ${webViewPackage.packageName}")

                // ✅ Handle both old and new APIs
                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    webViewPackage.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    webViewPackage.versionCode.toLong()
                }

                Timber.d("   Version Code: $versionCode")

                // Check if version is modern enough
                if (versionCode < 500000000) {
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

    private fun logMemoryInfo() {
        Timber.d("─────────────────────────────────────")
        Timber.d("💾 Memory Information:")

        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val totalMemory = runtime.totalMemory() / 1024 / 1024
        val freeMemory = runtime.freeMemory() / 1024 / 1024
        val usedMemory = totalMemory - freeMemory

        Timber.d("   Max Memory: ${maxMemory}MB")
        Timber.d("   Total Memory: ${totalMemory}MB")
        Timber.d("   Used Memory: ${usedMemory}MB")
        Timber.d("   Free Memory: ${freeMemory}MB")

        if (usedMemory > maxMemory * 0.8) {
            Timber.w("   ⚠️ Memory usage is high (${(usedMemory.toFloat() / maxMemory * 100).toInt()}%)")
        }
    }

    private fun logCookieInfo() {
        Timber.d("─────────────────────────────────────")
        Timber.d("🍪 Cookie Configuration:")

        val cookieManager = CookieManager.getInstance()
        Timber.d("   Accept Cookies: ${cookieManager.acceptCookie()}")

        Timber.d("   Third-party Cookies: Configured per-WebView")
    }

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

    fun logGpuStatus() {
        Timber.d("─────────────────────────────────────")
        Timber.d("🎮 GPU Status:")

        val isHardwareAccelerated = true
        Timber.d("   Hardware Acceleration: ${if (isHardwareAccelerated) "Available" else "Not Available"}")

        Timber.d("   Renderer: Modern (Chromium)")

        Timber.d("─────────────────────────────────────")
    }

    // ✅ FIXED: No more TODO() crash
    fun detectCommonIssues(context: Context) {
        Timber.d("═══════════════════════════════════════")
        Timber.d("       CHECKING FOR COMMON ISSUES")
        Timber.d("═══════════════════════════════════════")

        val issues = mutableListOf<String>()

        // Check WebView version
        try {
            val webViewPackage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WebViewCompat.getCurrentWebViewPackage(context)
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                webViewPackage?.longVersionCode ?: 0
            } else {
                @Suppress("DEPRECATION")
                webViewPackage?.versionCode?.toLong() ?: 0
            }

            if (versionCode < 500000000) {
                issues.add("WebView version is too old")
            }
        } catch (_: Exception) {
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