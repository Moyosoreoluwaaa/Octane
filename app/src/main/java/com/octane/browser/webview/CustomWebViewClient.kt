package com.octane.browser.webview

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.webkit.*
import com.octane.BuildConfig
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.webview.bridge.BridgeManager
import timber.log.Timber
import java.io.ByteArrayInputStream
import androidx.core.net.toUri

class CustomWebViewClient(
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

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString() ?: return false

        Timber.d("🔗 URL loading: $url")

        return when {
            url.startsWith("wc:") -> {
                Timber.d("🔌 WalletConnect URL detected")
                // TODO: Handle WalletConnect
                true
            }
            url.startsWith("dapp:") -> {
                Timber.d("🌐 dApp URL detected")
                // TODO: Handle custom dApp scheme
                true
            }
            url.startsWith("tel:") || url.startsWith("mailto:") -> {
                Timber.d("📞 Special URL detected: $url")
                // Let Android handle tel: and mailto:
                try {
                    android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = url.toUri()
                        view?.context?.startActivity(this)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to handle special URL")
                }
                true
            }
            else -> {
                // Allow normal navigation
                false
            }
        }
    }

    /**
     * ✅ ENHANCED: Better resource interception for complex sites
     */
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return null
        val method = request.method

        if (method == "OPTIONS") {
            // Handle CORS preflight
            val headers = mapOf(
                "Access-Control-Allow-Origin" to "*",
                "Access-Control-Allow-Methods" to "POST,GET,OPTIONS",
                "Access-Control-Allow-Headers" to "*"
            )
            return WebResourceResponse("text/plain", "UTF-8", 200, "OK", headers, ByteArrayInputStream(byteArrayOf()))
        }

        // Cache or modify resources if needed (e.g., for charts)
        return super.shouldInterceptRequest(view, request)
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler?,
        host: String?,
        realm: String?
    ) {
        // TODO: Handle auth if needed
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
            browserViewModel.onError("Error loading page: $description")
        } else {
            // Log subresource errors
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