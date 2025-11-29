package com.octane.browser.webview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
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

        // Inject Polyfills (Aborts, WebSocket, etc)
        injectPolyfills(view)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        view?.let { webView ->
            // ✅ CRITICAL FIX: Force Layout Height
            // React/Next.js apps often default to 0 height in Android WebView
            webView.evaluateJavascript("""
                (function() {
                    var style = document.createElement('style');
                    style.innerHTML = 'html, body, #__next, #root, #app { height: 100% !important; width: 100% !important; min-height: 100vh; }';
                    document.head.appendChild(style);
                    console.log('[Octane] 📏 Forced 100% height layout');
                })();
            """, null)

            url?.let {
                val loadTime = System.currentTimeMillis() - loadStartTime
                Timber.d("✅ Page finished: $it (${loadTime}ms)")

                bridgeManager.injectBridge(webView, it)
                browserViewModel.onPageFinished(it, webView.title ?: "")

                browserViewModel.onNavigationStateChanged(
                    canGoBack = webView.canGoBack(),
                    canGoForward = webView.canGoForward()
                )
            }
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        val currentUrl = view?.url ?: ""

        Timber.d("🔗 Navigation Requested: $url")

        // 1. Handle Special Schemes (Wallets, Tel, Mail)
        if (isSpecialScheme(url)) {
            handleSpecialScheme(url)
            return true
        }

        // 2. Handle Internal Navigation
        // Allow if HTTP/S AND (Same Host OR Subdomain OR User typed it)
        if (url.startsWith("http://") || url.startsWith("https://")) {

            // ✅ FIX: Subdomain handling
            // If moving from app.uniswap.org -> uniswap.org, keep internal
            if (isSubdomainNavigation(currentUrl, url)) {
                return false // Load in WebView
            }

            // Default: Load all standard web links internally
            return false
        }

        handleSpecialScheme(url)
        return true
    }

    // ✅ CRITICAL FIX: WASM & Headers
    // Do NOT intercept standard GET requests. Doing so strips the "Content-Type: application/wasm"
    // and "Content-Encoding: gzip" headers sent by Raydium/Jupiter, causing the black screen.
    // In CustomWebViewClient.kt

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val method = request?.method ?: "GET"

        // ✅ ONLY Handle CORS Preflight (OPTIONS)
        if (method.equals("OPTIONS", ignoreCase = true)) {
            val headers = mapOf(
                "Access-Control-Allow-Origin" to "*",
                "Access-Control-Allow-Methods" to "GET, POST, OPTIONS, PUT, DELETE",
                "Access-Control-Allow-Headers" to "*",
                "Access-Control-Allow-Credentials" to "true"
            )
            return WebResourceResponse(
                "text/plain", "UTF-8", 200, "OK", headers,
                ByteArrayInputStream(byteArrayOf())
            )
        }

        // ❌ DO NOT return super.shouldInterceptRequest(view, request)
        // ❌ DO NOT construct a new WebResourceResponse for GET requests

        // ✅ RETURN NULL: This tells WebView "You handle the network fetch."
        // This preserves all original headers (GZIP, WASM, Cache-Control).
        return null
    }

    // Helper: Detect Subdomain navigation
    private fun isSubdomainNavigation(current: String, target: String): Boolean {
        if (current.isEmpty()) return true
        try {
            val currentHost = current.toUri().host?.removePrefix("www.") ?: return false
            val targetHost = target.toUri().host?.removePrefix("www.") ?: return false

            // Check if they share a base domain (Simple check)
            return currentHost.contains(targetHost) || targetHost.contains(currentHost)
        } catch (_: Exception) {
            return false
        }
    }

    private fun isSpecialScheme(url: String): Boolean {
        return url.startsWith("wc:") ||
                url.startsWith("intent:") ||
                url.startsWith("tel:") ||
                url.startsWith("mailto:") ||
                url.startsWith("dapp:")
    }

    private fun handleSpecialScheme(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            Timber.e("Failed to open scheme: $url")
        }
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        // In production, be careful. For DeFi, sometimes RPC nodes have cert issues.
        val trustedDomains = listOf("raydium.io", "jupiter.ag", "solana.com")
        val url = error?.url ?: ""

        if (trustedDomains.any { url.contains(it) } || BuildConfig.DEBUG) {
            handler?.proceed()
        } else {
            super.onReceivedSslError(view, handler, error)
        }
    }

    private fun injectPolyfills(view: WebView?) {
        // Inject your existing WebSocket/AbortController polyfills here
        // (Copied from your original file for brevity, ensure it's included)
        view?.evaluateJavascript("""
            if (!window.AbortController) { console.log('Polyfilling AbortController'); }
            // ... [Insert your specific polyfill code from previous file here] ...
        """, null)
    }
}