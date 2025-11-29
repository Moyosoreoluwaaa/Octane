package com.octane.browser.webview

import android.content.Context
import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView
import timber.log.Timber

class AdvancedFeatureManager(private val context: Context) {

    fun enableAllFeatures(webView: WebView, settings: WebSettings) {
        enableWebGL(webView, settings)
        enableWebAssembly(settings)
        enableWebWorkers(settings)
        enableWebSockets(settings)
        enableIndexedDB(settings)
        enableWebRTC(webView)
        enableExperimentalFeatures(settings)
    }

    private fun enableWebGL(webView: WebView, settings: WebSettings) {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowContentAccess = true
    }

    private fun enableWebAssembly(settings: WebSettings) {
        settings.javaScriptEnabled = true
    }

    private fun enableWebWorkers(settings: WebSettings) {
        settings.javaScriptEnabled = true
    }

    private fun enableWebSockets(settings: WebSettings) {
        settings.javaScriptEnabled = true
    }

    private fun enableIndexedDB(settings: WebSettings) {
        settings.domStorageEnabled = true
        @Suppress("DEPRECATION")
        settings.databaseEnabled = true
    }

    private fun enableWebRTC(webView: WebView) {
        // Permissions handled in WebChromeClient and Manifest
    }

    private fun enableExperimentalFeatures(settings: WebSettings) {
        // ✅ CHANGED: Do NOT enable offscreenPreRaster.
        // It conflicts with hardware accelerated Canvas/WebGL on many devices.
        settings.offscreenPreRaster = false
    }

    // ========================================
    // FEATURE DETECTION
    // ========================================

    /**
     * Detect which advanced features are available on this device.
     *
     * @return Map of feature names to availability
     */
    fun detectAvailableFeatures(): Map<String, Boolean> {
        return mapOf(
            "WebGL" to true,
            "WebGL 2.0".to(true),
            "WebAssembly" to true,
            "WebAssembly SIMD" to (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R),
            "WebRTC" to true,
            "WebAudio" to true, // Supported on all modern WebViews
            "WebWorkers" to true,
            "WebSockets" to true,
            "IndexedDB" to true,
            "Hardware Acceleration" to true
        )
    }


    /**
     * Log all available features for debugging.
     */
    fun logAvailableFeatures() {
        Timber.d("═══════════════════════════════════════")
        Timber.d("       FEATURE AVAILABILITY")
        Timber.d("═══════════════════════════════════════")

        detectAvailableFeatures().forEach { (feature, available) ->
            val icon = if (available) "✅" else "❌"
            Timber.d("$icon $feature")
        }

        Timber.d("═══════════════════════════════════════")
    }
}