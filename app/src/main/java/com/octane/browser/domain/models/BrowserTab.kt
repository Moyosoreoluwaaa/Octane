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
    val screenshot: Bitmap? = null // ✅ NEW: Tab preview
)