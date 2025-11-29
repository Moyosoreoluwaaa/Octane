package com.octane.browser.utils

import android.webkit.WebView
import timber.log.Timber

/**
 * ✅ FIXED: Removed invalid CSS selectors that crash WebView
 *
 * Original issues:
 * - :contains() pseudo-selector is jQuery-only (not valid CSS)
 * - Caused "Uncaught SyntaxError" in production
 */
object WebViewDiagnosticTool {

    /**
     * Run comprehensive diagnostics on a loaded page
     */
    fun runPageDiagnostics(webView: WebView, onComplete: (DiagnosticResult) -> Unit) {
        Timber.d("🔍 Running page diagnostics...")

        webView.evaluateJavascript("""
            (function() {
                const body = document.body;
                const html = document.documentElement;
                
                // 1. Content Detection
                const hasContent = body && body.innerHTML.trim().length > 0;
                const contentLength = body ? body.innerHTML.length : 0;
                const bodyVisible = body ? window.getComputedStyle(body).display !== 'none' : false;
                const docHeight = Math.max(
                    body?.scrollHeight || 0,
                    body?.offsetHeight || 0,
                    html?.clientHeight || 0,
                    html?.scrollHeight || 0,
                    html?.offsetHeight || 0
                );
                
                // 2. Canvas/WebGL Detection
                const canvases = document.querySelectorAll('canvas');
                const canvasInfo = Array.from(canvases).map(c => {
                    let contextType = 'none';
                    if (c.getContext('2d')) contextType = '2d';
                    else if (c.getContext('webgl') || c.getContext('experimental-webgl')) contextType = 'webgl';
                    else if (c.getContext('webgl2')) contextType = 'webgl2';
                    
                    return {
                        width: c.width,
                        height: c.height,
                        context: contextType,
                        visible: c.offsetParent !== null
                    };
                });
                
                // 3. Framework Detection
                const hasReactRoot = !!document.querySelector('[data-reactroot], #root, #app, #__next, [data-react-helmet]');
                const hasVueRoot = !!document.querySelector('[data-v-app], [data-vue-app], #app');
                const hasAngularRoot = !!document.querySelector('[ng-app], [ng-version]');
                
                // 4. Resource Detection
                const scripts = document.querySelectorAll('script[src]');
                const stylesheets = document.querySelectorAll('link[rel="stylesheet"]');
                const images = document.querySelectorAll('img');
                
                // 5. Error Detection
                const errors = window.__octaneErrors || [];
                const hasWebGLError = errors.some(e => e.type === 'webglcontextlost');
                const hasJSError = errors.some(e => e.type === 'error' || e.type === 'console.error');
                
                // 6. Visibility Detection
                const isDocumentVisible = !document.hidden;
                const bodyOpacity = body ? parseFloat(window.getComputedStyle(body).opacity) : 1;
                const bodyZIndex = body ? window.getComputedStyle(body).zIndex : 'auto';
                
                // ✅ FIXED: Removed invalid :contains() selector
                // 7. UI Component Detection (using valid selectors)
                const hasChakraUI = !!document.querySelector('[class*="chakra"]');
                const hasTradingViewChart = !!document.querySelector('iframe[src*="tradingview"], #swap-tv-chart iframe');
                
                // ✅ FIXED: Check for wallet button by common class patterns
                const hasWalletButton = !!(
                    document.querySelector('button[class*="wallet"]') ||
                    document.querySelector('button[class*="connect"]') ||
                    document.querySelector('[data-testid*="wallet"]') ||
                    document.querySelector('[aria-label*="wallet" i]')
                );
                
                return JSON.stringify({
                    // Content
                    hasContent: hasContent,
                    contentLength: contentLength,
                    bodyVisible: bodyVisible,
                    docHeight: docHeight,
                    
                    // Canvas
                    canvasCount: canvases.length,
                    canvasInfo: canvasInfo,
                    
                    // Frameworks
                    framework: hasReactRoot ? 'React' : (hasVueRoot ? 'Vue' : (hasAngularRoot ? 'Angular' : 'Unknown')),
                    hasReactRoot: hasReactRoot,
                    
                    // Resources
                    scriptCount: scripts.length,
                    stylesheetCount: stylesheets.length,
                    imageCount: images.length,
                    
                    // Errors
                    errorCount: errors.length,
                    errors: errors.slice(0, 5), // Only first 5 errors
                    hasWebGLError: hasWebGLError,
                    hasJSError: hasJSError,
                    
                    // Visibility
                    isDocumentVisible: isDocumentVisible,
                    bodyOpacity: bodyOpacity,
                    bodyZIndex: bodyZIndex,
                    
                    // UI Components
                    hasChakraUI: hasChakraUI,
                    hasTradingViewChart: hasTradingViewChart,
                    hasWalletButton: hasWalletButton,
                    
                    // Meta
                    compatMode: document.compatMode,
                    readyState: document.readyState,
                    title: document.title,
                    url: window.location.href,
                    timestamp: Date.now()
                });
            })();
        """) { result ->
            if (result != null && result != "null") {
                try {
                    val diagnosticResult = parseDiagnosticResult(result)
                    logDiagnosticResult(diagnosticResult)
                    onComplete(diagnosticResult)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse diagnostic result")
                    onComplete(DiagnosticResult.error("Parse error: ${e.message}"))
                }
            } else {
                onComplete(DiagnosticResult.error("No result returned"))
            }
        }
    }

    private fun parseDiagnosticResult(json: String): DiagnosticResult {
        // Simple parsing - in production you'd use a proper JSON parser
        val hasContent = json.contains("\"hasContent\":true")
        val hasErrors = !json.contains("\"errorCount\":0")
        val hasCanvas = !json.contains("\"canvasCount\":0")
        val hasReact = json.contains("\"hasReactRoot\":true")
        val bodyVisible = json.contains("\"bodyVisible\":true")

        return DiagnosticResult(
            isBlank = !hasContent,
            hasErrors = hasErrors,
            hasCanvas = hasCanvas,
            hasReact = hasReact,
            bodyVisible = bodyVisible,
            rawJson = json
        )
    }

    private fun logDiagnosticResult(result: DiagnosticResult) {
        Timber.d("╔═══════════════════════════════════════╗")
        Timber.d("║     WEBVIEW DIAGNOSTIC RESULTS        ║")
        Timber.d("╚═══════════════════════════════════════╝")

        if (result.isBlank) {
            Timber.e("❌ PAGE IS BLANK")
        } else {
            Timber.d("✅ Page has content")
        }

        if (!result.bodyVisible) {
            Timber.e("❌ BODY IS NOT VISIBLE")
        } else {
            Timber.d("✅ Body is visible")
        }

        if (result.hasErrors) {
            Timber.e("❌ PAGE HAS ERRORS")
        } else {
            Timber.d("✅ No errors detected")
        }

        if (result.hasCanvas) {
            Timber.d("✅ Canvas elements found (charts likely working)")
        } else {
            Timber.w("⚠️ No canvas elements found")
        }

        if (result.hasReact) {
            Timber.d("✅ React framework detected")
        } else {
            Timber.w("⚠️ React not detected")
        }

        Timber.d("────────────────────────────────────────")
        Timber.d("Full diagnostic data: ${result.rawJson.take(200)}...")
        Timber.d("═══════════════════════════════════════")
    }

    /**
     * ✅ FIXED: Quick test for DeFi sites (removed invalid selectors)
     */
    fun testRaydiumPage(webView: WebView) {
        Timber.d("🧪 Running DeFi-specific tests...")

        webView.evaluateJavascript("""
            (function() {
                const tests = {
                    chakraUI: !!document.querySelector('[class*="chakra"]'),
                    // ✅ FIXED: Valid wallet button detection
                    walletButton: !!(
                        document.querySelector('button[class*="wallet"]') ||
                        document.querySelector('button[class*="connect"]') ||
                        document.querySelector('[data-testid*="wallet"]')
                    ),
                    tradingView: !!document.querySelector('#swap-tv-chart iframe'),
                    tokenInput: !!document.querySelector('input[name="swap"]'),
                    // ✅ FIXED: Check for swap button by aria-label or class
                    swapButton: !!(
                        document.querySelector('button[aria-label*="swap" i]') ||
                        document.querySelector('button[class*="swap"]')
                    ),
                    poolInfo: !!document.querySelector('[class*="pool"]'),
                    errors: (window.__octaneErrors || []).length
                };
                
                return JSON.stringify(tests);
            })();
        """) { result ->
            Timber.d("DeFi tests result: $result")

            if (result?.contains("\"tradingView\":true") == true) {
                Timber.d("✅ TradingView chart detected")
            } else {
                Timber.e("❌ TradingView chart NOT detected")
            }

            if (result?.contains("\"walletButton\":true") == true) {
                Timber.d("✅ Wallet button detected")
            } else {
                Timber.e("❌ Wallet button NOT detected")
            }
        }
    }

    /**
     * Force a page repaint/reflow
     */
    fun forceRepaint(webView: WebView) {
        Timber.d("🔄 Forcing page repaint...")

        webView.evaluateJavascript("""
            (function() {
                const body = document.body;
                const html = document.documentElement;
                
                // Method 1: Toggle display
                body.style.display = 'none';
                body.offsetHeight; // Force reflow
                body.style.display = '';
                
                // Method 2: Trigger resize event
                window.dispatchEvent(new Event('resize'));
                
                // Method 3: Force style recalculation
                html.style.transform = 'translateZ(0)';
                setTimeout(() => {
                    html.style.transform = '';
                }, 0);
                
                // Method 4: Trigger React re-render (if React is present)
                if (window.React) {
                    try {
                        const evt = new Event('storage');
                        window.dispatchEvent(evt);
                    } catch (e) {}
                }
                
                return 'Repaint forced';
            })();
        """) { result ->
            Timber.d("Repaint result: $result")
        }
    }

    data class DiagnosticResult(
        val isBlank: Boolean,
        val hasErrors: Boolean,
        val hasCanvas: Boolean,
        val hasReact: Boolean,
        val bodyVisible: Boolean,
        val rawJson: String
    ) {
        companion object {
            fun error(message: String) = DiagnosticResult(
                isBlank = true,
                hasErrors = true,
                hasCanvas = false,
                hasReact = false,
                bodyVisible = false,
                rawJson = "{\"error\": \"$message\"}"
            )
        }
    }
}