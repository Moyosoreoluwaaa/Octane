package com.octane.browser.domain.models

import android.graphics.Bitmap

// Dummy data structure for Quick Access (replace with actual repository later)
data class QuickAccessLink(
    val id: Int,
    val url: String,
    val title: String,
    val favicon: Bitmap? = null
)