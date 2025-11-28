package com.octane.browser.utils


import android.webkit.WebView
import timber.log.Timber

/**
 * ✅ DRIFT.TRADE SPECIFIC DIAGNOSTICS
 *
 * Run these checks when drift.trade loads with blank screen
 */
object DriftTradeDiagnostics {

    /**
     * Run comprehensive checks on the loaded page
     */
    fun runPageDiagnostics(webView: WebView) {
        Timber.d("═══════════════════════════════════════")
        Timber.d("    DRIFT.TRADE PAGE DIAGNOSTICS")
        Timber.d("═══════════════════════════════════════")

        // Check 1: Is document ready?
        webView.evaluateJavascript(
            "(function() { return document.readyState; })();",
            { result ->
                Timber.d("📄 Document State: $result")
                if (result != "\"complete\"") {
                    Timber.w("⚠️ Document not fully loaded!")
                }
            }
        )

        // Check 2: Is there any content in body?
        webView.evaluateJavascript(
            "(function() { return document.body.innerHTML.length; })();",
            { result ->
                val length = result?.replace("\"", "")?.toIntOrNull() ?: 0
                Timber.d("📄 Body content length: $length characters")

                if (length < 100) {
                    Timber.e("❌ Almost no content in body! Rendering failed.")
                } else if (length < 1000) {
                    Timber.w("⚠️ Very little content. Possible partial load.")
                } else {
                    Timber.d("✅ Good content length")
                }
            }
        )

        // Check 3: Are there JavaScript errors?
        webView.evaluateJavascript(
            """
            (function() {
                var errors = [];
                window.addEventListener('error', function(e) {
                    errors.push(e.message);
                });
                return errors.length > 0 ? errors.join('; ') : 'No errors';
            })();
            """.trimIndent(),
            { result ->
                Timber.d("🐛 JavaScript Errors: $result")
            }
        )

        // Check 4: Is React loaded?
        webView.evaluateJavascript(
            "(function() { return typeof React !== 'undefined' ? 'Yes' : 'No'; })();",
            { result ->
                Timber.d("⚛️ React loaded: $result")
                if (result == "\"No\"") {
                    Timber.e("❌ React not loaded! This is a React app.")
                }
            }
        )

        // Check 5: Check root element
        webView.evaluateJavascript(
            "(function() { var root = document.getElementById('root'); return root ? 'Found' : 'Missing'; })();",
            { result ->
                Timber.d("🎯 Root element: $result")
                if (result == "\"Missing\"") {
                    Timber.e("❌ Root element missing! React can't mount.")
                }
            }
        )

        // Check 6: Check if root has content
        webView.evaluateJavascript(
            "(function() { var root = document.getElementById('root'); return root ? root.innerHTML.length : 0; })();",
            { result ->
                val length = result?.toIntOrNull() ?: 0
                Timber.d("🎯 Root content length: $length characters")

                if (length == 0) {
                    Timber.e("❌ Root element is EMPTY! React failed to render.")
                }
            }
        )

        // Check 7: Check console for errors
        webView.evaluateJavascript(
            """
            (function() {
                var errors = [];
                var originalError = console.error;
                console.error = function() {
                    errors.push(Array.from(arguments).join(' '));
                    originalError.apply(console, arguments);
                };
                return errors.length;
            })();
            """.trimIndent(),
            { result ->
                Timber.d("📋 Console errors captured: $result")
            }
        )

        // Check 8: Check for Web3/Wallet integration issues
        webView.evaluateJavascript(
            "(function() { return typeof window.ethereum !== 'undefined' ? 'Present' : 'Missing'; })();",
            { result ->
                Timber.d("🦊 window.ethereum: $result")
                if (result == "\"Missing\"") {
                    Timber.w("⚠️ No wallet provider detected")
                }
            }
        )

        // Check 9: Check for critical CSS
        webView.evaluateJavascript(
            "(function() { return document.styleSheets.length; })();",
            { result ->
                val count = result?.toIntOrNull() ?: 0
                Timber.d("🎨 Stylesheets loaded: $count")

                if (count == 0) {
                    Timber.e("❌ No stylesheets loaded! Page will be unstyled.")
                }
            }
        )

        // Check 10: Check network state
        webView.evaluateJavascript(
            "(function() { return navigator.onLine ? 'Online' : 'Offline'; })();",
            { result ->
                Timber.d("🌐 Network state: $result")
            }
        )

        Timber.d("═══════════════════════════════════════")
    }

    /**
     * Force refresh the page with cache bypass
     */
    fun forceRefresh(webView: WebView) {
        Timber.d("🔄 Force refreshing page with cache bypass...")
        webView.clearCache(true)
        webView.reload()
    }

    /**
     * Inject a test div to see if DOM manipulation works
     */
    fun injectTestContent(webView: WebView) {
        Timber.d("💉 Injecting test content...")

        val testScript = """
            (function() {
                var testDiv = document.createElement('div');
                testDiv.id = 'octane-test';
                testDiv.style.cssText = 'position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); background: red; color: white; padding: 20px; z-index: 99999; font-size: 20px;';
                testDiv.textContent = 'OCTANE TEST: If you see this, DOM manipulation works!';
                document.body.appendChild(testDiv);
                
                setTimeout(function() {
                    testDiv.remove();
                }, 5000);
                
                return 'Test div injected';
            })();
        """.trimIndent()

        webView.evaluateJavascript(testScript) { result ->
            Timber.d("💉 Injection result: $result")
        }
    }

    /**
     * Get full diagnostic report
     */
    fun getFullReport(webView: WebView, callback: (String) -> Unit) {
        val report = StringBuilder()
        report.appendLine("═══════════════════════════════════════")
        report.appendLine("    FULL DIAGNOSTIC REPORT")
        report.appendLine("═══════════════════════════════════════")

        // Run all checks and compile report
        webView.evaluateJavascript(
            """
            (function() {
                var report = {
                    url: window.location.href,
                    readyState: document.readyState,
                    bodyLength: document.body.innerHTML.length,
                    title: document.title,
                    hasReact: typeof React !== 'undefined',
                    hasRoot: document.getElementById('root') !== null,
                    rootLength: document.getElementById('root') ? document.getElementById('root').innerHTML.length : 0,
                    stylesheets: document.styleSheets.length,
                    scripts: document.scripts.length,
                    online: navigator.onLine,
                    userAgent: navigator.userAgent
                };
                return JSON.stringify(report);
            })();
            """.trimIndent(),
            { result ->
                report.appendLine("JavaScript Report:")
                report.appendLine(result)
                report.appendLine("═══════════════════════════════════════")

                callback(report.toString())
                Timber.d(report.toString())
            }
        )
    }
}