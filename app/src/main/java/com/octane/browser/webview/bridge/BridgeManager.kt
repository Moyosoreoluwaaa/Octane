package com.octane.browser.webview.bridge

import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONObject
import timber.log.Timber

class BridgeManager(
    private val walletBridge: WalletBridge
) {

    fun createJavaScriptInterface(): JavaScriptBridge {
        return JavaScriptBridge(walletBridge)
    }

    class JavaScriptBridge(private val walletBridge: WalletBridge) {
        @JavascriptInterface
        fun postMessage(message: String) {
            try {
                val json = JSONObject(message)
                val method = json.optString("method")
                val id = json.optInt("id")

                when (method) {
                    "eth_requestAccounts" -> walletBridge.requestAccounts(id)
                    "eth_accounts" -> walletBridge.getAccounts(id)
                    "eth_chainId" -> walletBridge.getChainId(id)
                    "eth_sendTransaction" -> walletBridge.sendTransaction(id, json)
                    "personal_sign" -> walletBridge.personalSign(id, json)
                    else -> {
                        // Error handling - note: can't call sendError from here
                        Timber.tag("BridgeManager").e("Method not supported: $method")
                    }
                }
            } catch (e: Exception) {
                Timber.tag("BridgeManager").e(e, "Error handling message")
            }
        }
    }

    fun injectBridge(webView: WebView, url: String) {
        // Inject Ethereum provider
        val ethereumScript = buildEthereumProviderScript()
        webView.evaluateJavascript(ethereumScript, null)
        
        // Inject Solana provider (if needed)
        // val solanaScript = buildSolanaProviderScript()
        // webView.evaluateJavascript(solanaScript, null)
    }
    
    private fun buildEthereumProviderScript(): String {
        return """
            (function() {
                if (window.ethereum) return; // Already injected
                
                window.ethereum = {
                    isMetaMask: true,
                    isOctane: true,
                    
                    request: function(args) {
                        return new Promise((resolve, reject) => {
                            const id = Math.floor(Math.random() * 1000000);
                            
                            // Store callback
                            window._octaneCallbacks = window._octaneCallbacks || {};
                            window._octaneCallbacks[id] = { resolve, reject };
                            
                            // Send to native
                            AndroidBridge.postMessage(JSON.stringify({
                                id: id,
                                method: args.method,
                                params: args.params || []
                            }));
                        });
                    },
                    
                    // Legacy methods
                    send: function(method, params) {
                        return this.request({ method, params });
                    },
                    
                    sendAsync: function(payload, callback) {
                        this.request(payload)
                            .then(result => callback(null, { result }))
                            .catch(error => callback(error, null));
                    }
                };
                
                // Emit connected event
                window.dispatchEvent(new Event('ethereum#initialized'));
            })();
        """.trimIndent()
    }
    
    fun sendResponse(webView: WebView, id: Int, result:Any) {
        val script = """
            (function() {
                const callback = window._octaneCallbacks && window._octaneCallbacks[$id];
                if (callback) {
                    callback.resolve($result);
                    delete window._octaneCallbacks[$id];
                }
            })();
        """.trimIndent()
        
        webView.post {
            webView.evaluateJavascript(script, null)
        }
    }
    
    fun sendError(id: Int, error: String) {
        // TODO: Send error to JavaScript callback
    }
}