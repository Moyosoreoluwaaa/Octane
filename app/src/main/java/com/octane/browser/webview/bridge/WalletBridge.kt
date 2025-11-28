package com.octane.browser.webview.bridge

import android.webkit.WebView
import org.json.JSONObject

class WalletBridge(/* REMOVE private val bridgeManager: BridgeManager */) {

    // 1. Change to lateinit var (must be set before use)
    private lateinit var bridgeManager: BridgeManager
    private var currentWebView: WebView? = null

    // 2. Add setter function for the BridgeManager
    fun setBridgeManager(manager: BridgeManager) {
        this.bridgeManager = manager
    }

    fun setWebView(webView: WebView) {
        currentWebView = webView
    }

    fun requestAccounts(requestId: Int) {
        // TODO: Show wallet connection dialog
        // For now, return mock address
        val mockAddress = """["0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"]"""
        currentWebView?.let {
            bridgeManager.sendResponse(it, requestId, mockAddress)
        }
    }

    fun getAccounts(requestId: Int) {
        // TODO: Get connected accounts from wallet
        val accounts = """["0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"]"""
        currentWebView?.let {
            bridgeManager.sendResponse(it, requestId, accounts)
        }
    }

    fun getChainId(requestId: Int) {
        // TODO: Get current chain ID from wallet
        val chainId = """"0x1"""" // Ethereum mainnet
        currentWebView?.let {
            bridgeManager.sendResponse(it, requestId, chainId)
        }
    }

    fun sendTransaction(requestId: Int, params: JSONObject) {
        // TODO: Show transaction confirmation dialog
        // Parse transaction params and send to wallet
        val txHash = """"0x123...abc""""
        currentWebView?.let {
            bridgeManager.sendResponse(it, requestId, txHash)
        }
    }

    fun personalSign(requestId: Int, params: JSONObject) {
        // TODO: Show sign message dialog
        val signature = """"0xabc...123""""
        currentWebView?.let {
            bridgeManager.sendResponse(it, requestId, signature)
        }
    }
}

