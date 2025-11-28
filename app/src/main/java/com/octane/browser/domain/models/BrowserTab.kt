package com.octane.browser.domain.models

import android.graphics.Bitmap

data class BrowserTab(
    val id: String,
    val url: String,
    val title: String,
    val favicon: Bitmap?,
    val timestamp: Long,
    val isActive: Boolean
)