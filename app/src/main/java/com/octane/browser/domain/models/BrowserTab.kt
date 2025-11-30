package com.octane.browser.domain.models

import android.graphics.Bitmap

/**
 * ✅ ENHANCED: Added screenshot storage
 */
data class BrowserTab(
    val id: String,
    val url: String,
    val title: String,
    val timestamp: Long,
    val isActive: Boolean = false,
    val favicon: Bitmap? = null,
    val screenshot: Bitmap? = null, // ✅ NEW: Tab preview
    // ✅ NEW: WebView state preservation
    val scrollX: Int = 0,              // Horizontal scroll position
    val scrollY: Int = 0,              // Vertical scroll position
    val canGoBack: Boolean = false,    // WebView back stack
    val canGoForward: Boolean = false, // WebView forward stack
    val progress: Int = 0,             // Loading progress
    val isLoading: Boolean = false,    // Loading state
    val isSecure: Boolean = false      // HTTPS indicator
)