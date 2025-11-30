package com.octane.browser.domain.models

import android.graphics.Bitmap

/**
 * ✅ Updated Domain Model for Quick Access
 */
data class QuickAccessLink(
    val id: Long = 0,
    val url: String,
    val title: String,
    val favicon: Bitmap? = null,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)