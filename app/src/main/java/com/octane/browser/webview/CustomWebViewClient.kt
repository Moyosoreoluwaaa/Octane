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

        // Inject Polyfills
        injectPolyfills(view)

        // ✅ NEW: Inject dark theme CSS for better compatibility
        injectDarkThemeHelpers(view)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        view?.let { webView ->
            // ✅ Force Layout Height (React/Next.js fix)
            webView.evaluateJavascript("""
                (function() {
                    var style = document.createElement('style');
                    style.innerHTML = 'html, body, #__next, #root, #app { height: 100% !important; width: 100% !important; min-height: 100vh; }';
                    document.head.appendChild(style);
                    console.log('[Octane] 📐 Forced 100% height layout');
                })();
            """, null)

            // ✅ NEW: Ensure dark theme is properly applied
            WebViewThemeManager.setupTheme(webView, context)

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

        // Handle Special Schemes
        if (isSpecialScheme(url)) {
            handleSpecialScheme(url)
            return true
        }

        // Handle Internal Navigation
        if (url.startsWith("http://") || url.startsWith("https://")) {
            if (isSubdomainNavigation(currentUrl, url)) {
                return false // Load in WebView
            }
            return false // Load all HTTP/S internally
        }

        handleSpecialScheme(url)
        return true
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val method = request?.method ?: "GET"

        // Only Handle CORS Preflight (OPTIONS)
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

        // ✅ Return null to preserve original headers (WASM, GZIP, etc.)
        return null
    }

    private fun isSubdomainNavigation(current: String, target: String): Boolean {
        if (current.isEmpty()) return true
        try {
            val currentHost = current.toUri().host?.removePrefix("www.") ?: return false
            val targetHost = target.toUri().host?.removePrefix("www.") ?: return false
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
        val trustedDomains = listOf("raydium.io", "jupiter.ag", "solana.com")
        val url = error?.url ?: ""

        if (trustedDomains.any { url.contains(it) } || BuildConfig.DEBUG) {
            handler?.proceed()
        } else {
            super.onReceivedSslError(view, handler, error)
        }
    }

    private fun injectPolyfills(view: WebView?) {
        view?.evaluateJavascript("""
            (function() {
                // AbortController Polyfill
                if (!window.AbortController) {
                    console.log('[Octane] Polyfilling AbortController');
                    window.AbortController = function() {
                        this.signal = { aborted: false };
                        this.abort = function() { this.signal.aborted = true; };
                    };
                }
                
                // WebSocket Polyfill enhancements
                if (window.WebSocket) {
                    const OriginalWebSocket = window.WebSocket;
                    window.WebSocket = function(url, protocols) {
                        console.log('[Octane] WebSocket connecting to:', url);
                        return new OriginalWebSocket(url, protocols);
                    };
                    window.WebSocket.prototype = OriginalWebSocket.prototype;
                }
            })();
        """, null)
    }

    /**
     * ✅ NEW: Inject dark theme CSS helpers
     *
     * This helps websites that don't properly implement prefers-color-scheme
     * by adding a fallback color-scheme meta tag
     */
    private fun injectDarkThemeHelpers(view: WebView?) {
        val isDarkMode = WebViewThemeManager.isDarkMode(context)

        if (isDarkMode) {
            view?.evaluateJavascript("""
                (function() {
                    // Check if color-scheme meta tag exists
                    var metaTag = document.querySelector('meta[name="color-scheme"]');
                    
                    if (!metaTag) {
                        console.log('[Octane] 🎨 Adding color-scheme meta tag');
                        metaTag = document.createElement('meta');
                        metaTag.name = 'color-scheme';
                        metaTag.content = 'dark light';
                        document.head.appendChild(metaTag);
                    } else {
                        console.log('[Octane] 🎨 Site already has color-scheme:', metaTag.content);
                    }
                    
                    // Log prefers-color-scheme detection
                    if (window.matchMedia) {
                        var darkModeQuery = window.matchMedia('(prefers-color-scheme: dark)');
                        console.log('[Octane] 🎨 prefers-color-scheme: dark =', darkModeQuery.matches);
                    }
                })();
            """, null)
        }
    }
}