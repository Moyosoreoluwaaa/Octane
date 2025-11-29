package com.octane.browser.webview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.webkit.*
import androidx.core.net.toUri
import com.octane.BuildConfig
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.utils.WebViewDiagnosticTool
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

        // ✅ ADD THIS: Inject WebSocket & Fetch AbortSignal polyfills (FIRST INJECTION)
        view?.evaluateJavascript(
            """
(function() {
    'use strict';
    
    console.log('[Octane] 🚀 Injecting WebSocket & Fetch AbortSignal polyfills...');
    
    // ✅ FIX #1: Polyfill AbortSignal for older WebView versions
    if (!window.AbortSignal || !window.AbortController) {
        console.warn('[Octane] ⚠️ AbortSignal missing, adding polyfill');
        
        class AbortSignal {
            constructor() {
                this.aborted = false;
                this.reason = undefined;
                this._listeners = [];
            }
            
            addEventListener(type, listener) {
                if (type === 'abort' && typeof listener === 'function') {
                    this._listeners.push(listener);
                }
            }
            
            removeEventListener(type, listener) {
                if (type === 'abort') {
                    this._listeners = this._listeners.filter(l => l !== listener);
                }
            }
            
            _triggerAbort(reason) {
                this.aborted = true;
                this.reason = reason;
                this._listeners.forEach(listener => {
                    try {
                        listener({ type: 'abort', target: this });
                    } catch (e) {
                        console.error('[Octane] AbortSignal listener error:', e);
                    }
                });
            }
        }
        
        class AbortController {
            constructor() {
                this.signal = new AbortSignal();
            }
            
            abort(reason) {
                if (!this.signal.aborted) {
                    this.signal._triggerAbort(reason);
                }
            }
        }
        
        window.AbortSignal = AbortSignal;
        window.AbortController = AbortController;
        console.log('[Octane] ✅ AbortSignal polyfill installed');
    }
    
    // ✅ FIX #2: Patch fetch() to handle AbortSignal properly
    const originalFetch = window.fetch;
    window.fetch = function(url, options) {
        // If there's an AbortSignal, ensure it's properly handled
        if (options && options.signal) {
            const signal = options.signal;
            
            // If already aborted, reject immediately
            if (signal.aborted) {
                return Promise.reject(new DOMException('The operation was aborted.', 'AbortError'));
            }
            
            // Wrap the fetch to listen for abort
            return new Promise((resolve, reject) => {
                const abortListener = () => {
                    reject(new DOMException('The operation was aborted.', 'AbortError'));
                };
                
                signal.addEventListener('abort', abortListener);
                
                originalFetch(url, options)
                    .then(response => {
                        signal.removeEventListener('abort', abortListener);
                        resolve(response);
                    })
                    .catch(error => {
                        signal.removeEventListener('abort', abortListener);
                        reject(error);
                    });
            });
        }
        
        return originalFetch(url, options);
    };
    
    // ✅ FIX #3: Patch WebSocket to handle connection issues gracefully
    const OriginalWebSocket = window.WebSocket;
    window.WebSocket = function(url, protocols) {
        console.log('[Octane] 🔌 WebSocket connecting:', url);
        
        const ws = new OriginalWebSocket(url, protocols);
        
        // Add automatic reconnection on error
        const originalOnError = ws.onerror;
        ws.onerror = function(event) {
            console.error('[Octane] ❌ WebSocket error:', url);
            
            if (originalOnError) {
                originalOnError.call(this, event);
            }
        };
        
        const originalOnClose = ws.onclose;
        ws.onclose = function(event) {
            console.warn('[Octane] 🔌 WebSocket closed:', url, 'Code:', event.code);
            
            if (originalOnClose) {
                originalOnClose.call(this, event);
            }
        };
        
        return ws;
    };
    
    // Copy over WebSocket static properties
    window.WebSocket.CONNECTING = OriginalWebSocket.CONNECTING;
    window.WebSocket.OPEN = OriginalWebSocket.OPEN;
    window.WebSocket.CLOSING = OriginalWebSocket.CLOSING;
    window.WebSocket.CLOSED = OriginalWebSocket.CLOSED;
    
    // ✅ FIX #4: Increase WebSocket timeout for slow connections
    window.__octaneWebSocketTimeout = 30000; // 30 seconds (default is 5s)
    
    console.log('[Octane] ✅ WebSocket & Fetch polyfills installed');
})();
""", null
        )

        // ✅ CRITICAL FIX: Enhanced pre-page injection (EXISTING CODE)
        view?.evaluateJavascript(
            """
        (function() {
            'use strict';
            
            console.log('[Octane] 🚀 Starting ENHANCED pre-page injection...');
            
            // ✅ FIX #1: Ensure proper document mode (prevent quirks mode)
            if (document.compatMode !== 'CSS1Compat') {
                console.warn('[Octane] ⚠️ Not in standards mode:', document.compatMode);
            }
            
            // ✅ FIX #2: Force UTF-8 encoding
            if (document.head && !document.querySelector('meta[charset]')) {
                const charset = document.createElement('meta');
                charset.setAttribute('charset', 'UTF-8');
                document.head.insertBefore(charset, document.head.firstChild);
                console.log('[Octane] ✅ Added UTF-8 charset');
            }
            
            // ✅ FIX #3: Enhanced WebGL context configuration
            const originalGetContext = HTMLCanvasElement.prototype.getContext;
            HTMLCanvasElement.prototype.getContext = function(type, attributes) {
                if (type === 'webgl' || type === 'webgl2' || type === 'experimental-webgl') {
                    attributes = attributes || {};
                    attributes.alpha = attributes.alpha !== false;
                    attributes.antialias = attributes.antialias !== false;
                    attributes.depth = attributes.depth !== false;
                    attributes.stencil = attributes.stencil !== false;
                    attributes.preserveDrawingBuffer = true;
                    attributes.powerPreference = 'high-performance';
                    attributes.failIfMajorPerformanceCaveat = false;
                    attributes.desynchronized = true;
                    
                    console.log('[Octane] 🎨 WebGL context requested:', type, attributes);
                    
                    const ctx = originalGetContext.call(this, type, attributes);
                    if (!ctx) {
                        console.error('[Octane] ❌ WebGL context creation FAILED');
                    } else {
                        console.log('[Octane] ✅ WebGL context created successfully');
                    }
                    return ctx;
                }
                return originalGetContext.call(this, type, attributes);
            };
            
            // ✅ FIX #4: Canvas rendering detection
            const originalToDataURL = HTMLCanvasElement.prototype.toDataURL;
            HTMLCanvasElement.prototype.toDataURL = function() {
                const result = originalToDataURL.apply(this, arguments);
                console.log('[Octane] 📸 Canvas toDataURL called, size:', this.width, 'x', this.height);
                return result;
            };
            
            // ✅ FIX #5: Track page visibility
            window.__octanePageVisible = true;
            document.addEventListener('visibilitychange', function() {
                window.__octanePageVisible = !document.hidden;
                console.log('[Octane] 👁️ Page visibility:', window.__octanePageVisible);
            });
            
            // ✅ FIX #6: Comprehensive error tracking
            window.__octaneErrors = [];
            
            window.addEventListener('error', function(e) {
                const error = {
                    type: 'error',
                    message: e.message,
                    source: e.filename,
                    line: e.lineno,
                    col: e.colno,
                    stack: e.error ? e.error.stack : null,
                    time: Date.now()
                };
                window.__octaneErrors.push(error);
                console.error('[Octane] ❌ Page error:', error);
            }, true);
            
            // ✅ FIX #7: WebGL context loss handling
            window.addEventListener('webglcontextlost', function(e) {
                console.error('[Octane] 💥 WebGL context LOST!', e);
                window.__octaneErrors.push({ 
                    type: 'webglcontextlost', 
                    message: 'WebGL context lost',
                    time: Date.now()
                });
                e.preventDefault(); // Try to restore
            }, false);
            
            window.addEventListener('webglcontextrestored', function(e) {
                console.log('[Octane] ✅ WebGL context RESTORED');
            }, false);
            
            // ✅ FIX #8: Intercept console errors
            const originalConsoleError = console.error;
            console.error = function(...args) {
                window.__octaneErrors.push({ 
                    type: 'console.error',
                    message: args.map(a => String(a)).join(' '),
                    time: Date.now()
                });
                return originalConsoleError.apply(console, args);
            };
            
            // ✅ FIX #9: Track render blocking resources
            window.__octaneResourcesLoaded = {
                css: 0,
                js: 0,
                fonts: 0,
                images: 0
            };
            
            const observer = new PerformanceObserver((list) => {
                for (const entry of list.getEntries()) {
                    const resource = entry.name;
                    if (resource.endsWith('.css')) window.__octaneResourcesLoaded.css++;
                    if (resource.endsWith('.js')) window.__octaneResourcesLoaded.js++;
                    if (resource.includes('font')) window.__octaneResourcesLoaded.fonts++;
                    if (resource.match(/\.(jpg|jpeg|png|gif|svg|webp)$/i)) window.__octaneResourcesLoaded.images++;
                }
            });
            observer.observe({ entryTypes: ['resource'] });
            
            // ✅ FIX #10: Force repaint if page is blank after 3 seconds
            setTimeout(function() {
                const body = document.body;
                if (body && body.innerHTML.trim().length === 0) {
                    console.warn('[Octane] ⚠️ Page appears blank, forcing repaint...');
                    body.style.display = 'none';
                    body.offsetHeight; // Force reflow
                    body.style.display = '';
                }
            }, 3000);
            
            console.log('[Octane] ✅ Enhanced pre-page injection complete');
            console.log('[Octane] 📊 Document mode:', document.compatMode);
            console.log('[Octane] 🔍 ReadyState:', document.readyState);
        })();
        """, null
        )
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

                // ✅ ENHANCED: Check page rendering status
                checkPageRenderStatus(webView)
            }
        }
    }

    // ✅ NEW: Comprehensive page render status check
    private fun checkPageRenderStatus(webView: WebView) {
        // Use the diagnostic tool
        WebViewDiagnosticTool.runPageDiagnostics(webView) { result ->
            if (result.isBlank) {
                Timber.e("⚠️ BLANK PAGE DETECTED!")
                Timber.e("   Diagnosis: ${result.rawJson.take(200)}")

                // Auto-retry: Force repaint after 2 seconds
                webView.postDelayed({
                    Timber.d("🔄 Auto-retry: Forcing repaint...")
                    WebViewDiagnosticTool.forceRepaint(webView)
                }, 2000)
            } else if (result.hasErrors) {
                Timber.e("❌ Page has errors but is visible")
            } else {
                Timber.d("✅ Page rendered successfully!")
            }
        }

        // Also run legacy check
        legacyCheckPageRenderStatus(webView)
    }

    // Keep the old method as fallback
    private fun legacyCheckPageRenderStatus(webView: WebView) {
        webView.evaluateJavascript(
            """
            (function() {
                const body = document.body;
                const html = document.documentElement;
                
                // Check if page has content
                const hasContent = body && body.innerHTML.trim().length > 0;
                const bodyVisible = body ? window.getComputedStyle(body).display !== 'none' : false;
                const docHeight = Math.max(
                    body?.scrollHeight || 0,
                    body?.offsetHeight || 0,
                    html?.clientHeight || 0,
                    html?.scrollHeight || 0,
                    html?.offsetHeight || 0
                );
                
                // Check for canvas elements
                const canvases = document.querySelectorAll('canvas');
                const canvasInfo = Array.from(canvases).map(c => ({
                    width: c.width,
                    height: c.height,
                    context: c.getContext('2d') ? '2d' : (c.getContext('webgl') ? 'webgl' : 'none')
                }));
                
                // Check for React/Vue root elements
                const hasReactRoot = !!document.querySelector('[data-reactroot], #root, #app, #__next');
                
                return JSON.stringify({
                    hasBody: !!body,
                    hasContent: hasContent,
                    bodyVisible: bodyVisible,
                    docHeight: docHeight,
                    canvasCount: canvases.length,
                    canvasInfo: canvasInfo,
                    hasReactRoot: hasReactRoot,
                    errors: window.__octaneErrors || [],
                    resourcesLoaded: window.__octaneResourcesLoaded || {},
                    compatMode: document.compatMode,
                    readyState: document.readyState,
                    pageVisible: window.__octanePageVisible !== false
                });
            })();
        """
        ) { result ->
            if (result != null && result != "null") {
                Timber.d("📊 Page render status: $result")

                // Parse and diagnose issues
                try {
                    val isBlank = result.contains("\"hasContent\":false") ||
                            result.contains("\"docHeight\":0")
                    val hasErrors = result.contains("\"errors\":[") &&
                            !result.contains("\"errors\":[]")

                    if (isBlank) {
                        Timber.w("⚠️ BLANK PAGE DETECTED!")
                        Timber.w("   Possible causes:")
                        Timber.w("   1. JavaScript failed to initialize")
                        Timber.w("   2. WebGL context creation failed")
                        Timber.w("   3. React/Vue app failed to mount")
                        Timber.w("   4. CSS is hiding content")

                        // Try to force a reload
                        webView.postDelayed({
                            Timber.d("🔄 Attempting to force render...")
                            webView.evaluateJavascript(
                                """
                                document.body?.style.display = 'none';
                                document.body?.offsetHeight;
                                document.body?.style.display = '';
                                window.dispatchEvent(new Event('resize'));
                            """, null
                            )
                        }, 1000)
                    }

                    if (hasErrors) {
                        Timber.e("❌ Page has JavaScript errors - check console")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse render status")
                }
            }
        }
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString() ?: return false

        Timber.d("🔗 Navigation Requested: $url")

        // Critical schemes -> External
        if (url.startsWith("wc:") ||
            url.startsWith("dapp:") ||
            url.startsWith("tel:") ||
            url.startsWith("mailto:") ||
            url.startsWith("intent:")
        ) {
            handleSpecialScheme(url)
            return true
        }

        // JavaScript and hash fragments -> Internal
        if (url.startsWith("javascript:") || url.contains("#")) {
            return false
        }

        // Standard web -> Internal
        if (url.startsWith("http://") || url.startsWith("https://")) {
            Timber.d("➡️ Standard web link, loading internally: $url")
            return false
        }

        // Fallback
        handleSpecialScheme(url)
        return true
    }

    // ✅ ADD THIS: Updated shouldInterceptRequest with Ultra-permissive CSP
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)
        val method = request.method

        // ✅ Handle CORS preflight
        if (method == "OPTIONS") {
            val headers = mapOf(
                "Access-Control-Allow-Origin" to "*",
                "Access-Control-Allow-Methods" to "POST,GET,OPTIONS,PUT,DELETE,PATCH",
                "Access-Control-Allow-Headers" to "*",
                "Access-Control-Max-Age" to "86400",
                "Access-Control-Allow-Credentials" to "true"
            )
            return WebResourceResponse(
                "text/plain", "UTF-8", 200, "OK", headers,
                ByteArrayInputStream(byteArrayOf())
            )
        }

        val response = super.shouldInterceptRequest(view, request)

        // ✅ Inject CORS and permissive CSP
        response?.let {
            val mutableHeaders = it.responseHeaders?.toMutableMap() ?: mutableMapOf()

            // CORS headers
            mutableHeaders["Access-Control-Allow-Origin"] = "*"
            mutableHeaders["Access-Control-Allow-Credentials"] = "true"
            mutableHeaders["Access-Control-Allow-Methods"] = "POST,GET,OPTIONS,PUT,DELETE,PATCH"
            mutableHeaders["Access-Control-Allow-Headers"] = "*"

            // ✅ CRITICAL: Ultra-permissive CSP for DeFi apps
            mutableHeaders["Content-Security-Policy"] =
                "default-src * 'unsafe-inline' 'unsafe-eval' data: blob: filesystem: ws: wss:; " +
                        "script-src * 'unsafe-inline' 'unsafe-eval' blob: data:; " +
                        "worker-src * blob: data:; " +
                        "connect-src * ws: wss: blob: data:; " + // ✅ WebSocket allowed
                        "img-src * data: blob: 'unsafe-inline'; " +
                        "media-src * blob: data:; " +
                        "font-src * data: blob:; " +
                        "style-src * 'unsafe-inline' blob: data:; " +
                        "frame-src * blob: data:; " +
                        "child-src * blob: data:;"

            // Remove restrictive headers
            mutableHeaders.remove("X-Frame-Options")
            mutableHeaders.remove("X-Content-Type-Options")

            return WebResourceResponse(
                it.mimeType,
                it.encoding,
                it.statusCode,
                it.reasonPhrase,
                mutableHeaders,
                it.data
            )
        }

        return response
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
        val url = error?.url ?: ""
        Timber.w("🔒 SSL Error: ${error?.primaryError} for $url")

        // ✅ Trusted DeFi domains
        val trustedDomains = listOf(
            "raydium.io",
            "drift.trade",
            "jupiter.ag",
            "solana.com",
            "rpc.ankr.com",
            "quicknode.pro",
            "helius-rpc.com",
            "projectserum.com",
            "mainnet-beta.solana.com",
            "api.raydium.io",
            "frontend.raydium.io"
        )

        val isTrustedDomain = trustedDomains.any { url.contains(it, ignoreCase = true) }

        if (isTrustedDomain || BuildConfig.DEBUG) {
            Timber.w("⚠️ Proceeding despite SSL error for trusted domain")
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
            browserViewModel.onError(
                "Error loading page: ${
                    getUserFriendlyErrorMessage(
                        errorCode,
                        description
                    )
                }"
            )
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
            Timber.e("💥 Render process gone! Crashed: ${detail?.didCrash()}")

            try {
                view?.destroy()
            } catch (e: Exception) {
                Timber.e(e, "Error destroying WebView after render process crash")
            }

            browserViewModel.onError("Page crashed. Please reload.")
            return true
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