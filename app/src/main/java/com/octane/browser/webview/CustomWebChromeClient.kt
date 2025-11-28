package com.octane.browser.webview

import android.graphics.Bitmap
import android.webkit.*
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
    
    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        // TODO: Show native Android AlertDialog
        result?.confirm()
        return true
    }
    
    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        // TODO: Show confirmation dialog
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
        // TODO: Show input dialog
        result?.confirm(defaultValue)
        return true
    }
    
    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        consoleMessage?.let {
            Timber.tag("WebView Console")
                .d("[${it.messageLevel()}] ${it.message()} (${it.sourceId()}:${it.lineNumber()})")
        }
        return true
    }
}
