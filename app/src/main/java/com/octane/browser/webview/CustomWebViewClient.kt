package com.octane.browser.webview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.webkit.*
import androidx.core.net.toUri
import com.octane.BuildConfig
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.webview.bridge.BridgeManager
import timber.log.Timber
import java.io.ByteArrayInputStream

class CustomWebViewClient(
    private val context: Context,
    private val browserViewModel: BrowserViewModel,
    private val bridgeManager: BridgeManager
) : WebViewClient() {

    // Track page load progress
    private var isPageLoading = false
    private var loadStartTime = 0L

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        isPageLoading = true
        loadStartTime = System.currentTimeMillis()

        url?.let {
            Timber.d("🌐 Page started: $it")
            browserViewModel.onPageStarted(it)
            browserViewModel.onNavigationStateChanged(
                canGoBack = view?.canGoBack() == true,
                canGoForward = view?.canGoForward() == true
            )
        }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        view?.let { webView ->
            url?.let {
                val loadTime = System.currentTimeMillis() - loadStartTime
                Timber.d("✅ Page finished: $it (${loadTime}ms)")

                // FIX: Viewport Injection (Crucial for complex React/DeFi apps like Drift)
                // Forces the web page to respect the device's width.
                webView.evaluateJavascript("""
                    javascript:(function() { 
                        var meta = document.querySelector('meta[name="viewport"]');
                        if (!meta) {
                            meta = document.createElement('meta');
                            meta.name = 'viewport'; 
                            document.getElementsByTagName('head')[0].appendChild(meta);
                        }
                        meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'; 
                    })()
                """, null)

                // Inject bridge after page loads
                bridgeManager.injectBridge(webView, it)

                val title = webView.title ?: ""
                browserViewModel.onPageFinished(it, title)

                browserViewModel.onNavigationStateChanged(
                    canGoBack = webView.canGoBack(),
                    canGoForward = webView.canGoForward()
                )

                isPageLoading = false
            }
        }
    }

    /**
     * ✅ RESTORED NAVIGATION LOGIC: All standard web links are now kept internal.
     */
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString() ?: return false

        Timber.d("🔗 Navigation Requested: $url")

        // 1. Critical Schemes (WalletConnect, Tel, Mail, Intents) -> External
        if (url.startsWith("wc:") || url.startsWith("dapp:") || url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("intent:")) {
            handleSpecialScheme(url)
            return true // Consumed: open external app/intent
        }

        // 2. Fix Hamburger Menus & SPA Routers
        if (url.startsWith("javascript:") || url.contains("#")) {
            return false // Let WebView handle it internally (Fixes menus/routing)
        }

        // 3. Standard Web (http:, https:) -> Internal (User's request)
        if (url.startsWith("http://") || url.startsWith("https://")) {
            Timber.d("➡️ Standard web link, loading internally: $url")
            return false // Return false to load in the current WebView
        }

        // Fallback for any other unrecognized custom scheme
        handleSpecialScheme(url)
        return true
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val method = request?.method

        if (method == "OPTIONS") {
            // Handle CORS preflight (essential for some dApp APIs)
            val headers = mapOf(
                "Access-Control-Allow-Origin" to "*",
                "Access-Control-Allow-Methods" to "POST,GET,OPTIONS",
                "Access-Control-Allow-Headers" to "*"
            )
            return WebResourceResponse("text/plain", "UTF-8", 200, "OK", headers, ByteArrayInputStream(byteArrayOf()))
        }

        return super.shouldInterceptRequest(view, request)
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler?,
        host: String?,
        realm: String?
    ) {
        handler?.cancel()
    }

    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        Timber.w("🔒 SSL Error: ${error?.primaryError} for ${error?.url}")

        if (BuildConfig.DEBUG) {
            // DEVELOPMENT ONLY
            Timber.w("⚠️ DEBUG: Proceeding despite SSL error")
            handler?.proceed()
        } else {
            handler?.cancel()
            browserViewModel.onError("Secure connection failed")
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)

        val errorCode = error?.errorCode ?: 0
        val description = error?.description?.toString() ?: "Unknown"
        val url = request?.url?.toString() ?: "unknown"

        if (request?.isForMainFrame == true) {
            Timber.e("❌ Main frame error [$errorCode]: $description")
            Timber.e("   URL: $url")
            browserViewModel.onError("Error loading page: ${getUserFriendlyErrorMessage(errorCode, description)}")
        } else {
            Timber.v("⚠️ Subresource error [$errorCode]: $url")
        }
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        super.onReceivedHttpError(view, request, errorResponse)

        val statusCode = errorResponse?.statusCode ?: 0
        val url = request?.url?.toString() ?: "unknown"

        if (request?.isForMainFrame == true && statusCode >= 400) {
            Timber.e("❌ HTTP $statusCode: $url")
            browserViewModel.onError("HTTP Error $statusCode")
        } else {
            Timber.v("ℹ️ HTTP $statusCode: ${request?.url?.path}")
        }
    }

    override fun onRenderProcessGone(
        view: WebView?,
        detail: RenderProcessGoneDetail?
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Timber.e("💥 Render process gone! Crashed: ${detail?.didCrash()}, Priority at exit: ${detail?.rendererPriorityAtExit()}")

            try {
                view?.destroy()
            } catch (e: Exception) {
                Timber.e(e, "Error destroying WebView after render process crash")
            }

            browserViewModel.onError("Page crashed. Please reload.")

            return true // We handled the crash
        }

        return super.onRenderProcessGone(view, detail)
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        super.onLoadResource(view, url)

        if (BuildConfig.DEBUG) {
            url?.let {
                when {
                    it.endsWith(".wasm") -> Timber.v("⚡ WASM: ${it.substringAfterLast("/")}")
                    it.endsWith(".woff") || it.endsWith(".woff2") ->
                        Timber.v("🔤 Font: ${it.substringAfterLast("/")}")
                }
            }
        }
    }

    private fun handleSpecialScheme(url: String) {
        try {
            Timber.d("🚀 Opening special scheme link externally: $url")
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Failed to handle scheme: $url")
        }
    }


    private fun getUserFriendlyErrorMessage(errorCode: Int, description: String): String {
        return when (errorCode) {
            ERROR_HOST_LOOKUP -> "Cannot find server. Check your internet connection."
            ERROR_TIMEOUT -> "Connection timeout. Server is not responding."
            ERROR_CONNECT -> "Cannot connect to server."
            ERROR_FAILED_SSL_HANDSHAKE -> "Secure connection failed."
            ERROR_BAD_URL -> "Invalid URL."
            ERROR_UNSUPPORTED_SCHEME -> "This type of link is not supported."
            ERROR_IO -> "Network error occurred."
            ERROR_FILE_NOT_FOUND -> "Page not found (404)."
            ERROR_TOO_MANY_REQUESTS -> "Too many requests. Please try again later."
            ERROR_UNSAFE_RESOURCE -> "This page contains unsafe content."
            else -> description
        }
    }
}