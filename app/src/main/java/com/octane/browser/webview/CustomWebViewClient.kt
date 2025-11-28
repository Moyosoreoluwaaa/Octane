package com.octane.browser.webview

import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.octane.browser.presentation.viewmodels.BrowserViewModel
import com.octane.browser.webview.bridge.BridgeManager

class CustomWebViewClient(
    private val browserViewModel: BrowserViewModel,
    private val bridgeManager: BridgeManager
) : WebViewClient() {
    
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        url?.let {
            browserViewModel.onPageStarted(it)
            browserViewModel.onNavigationStateChanged(
                canGoBack = view?.canGoBack() == true,
                canGoForward = view?.canGoForward() == true
            )
        }
    }
    
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        
        // Inject Web3 bridge
        view?.let { webView ->
            url?.let { 
                bridgeManager.injectBridge(webView, it)
                
                // Get page title and notify ViewModel
                val title = webView.title ?: ""
                browserViewModel.onPageFinished(it, title)
                
                // Update navigation state
                browserViewModel.onNavigationStateChanged(
                    canGoBack = webView.canGoBack(),
                    canGoForward = webView.canGoForward()
                )
            }
        }
    }
    
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString() ?: return false
        
        return when {
            // Custom schemes
            url.startsWith("wc:") -> {
                // TODO: Handle WalletConnect
                true
            }
            url.startsWith("dapp:") -> {
                // TODO: Handle custom dApp scheme
                true
            }
            else -> {
                // Normal navigation
                view?.loadUrl(url)
                false
            }
        }
    }
    
    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)

        val errorMessage = error?.description?.toString() ?: "Unknown error"
        browserViewModel.onError(errorMessage)
    }
    
    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        // SECURITY: Block SSL errors by default
        handler?.cancel()
        browserViewModel.onError("SSL Error: Insecure connection")
    }
    
    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        // Log but don't show to user (often from subresources)
    }
}
