package com.octane.browser.data.mappers

import com.octane.browser.data.local.db.entity.BrowserTabEntity
import com.octane.browser.domain.models.BrowserTab

/**
 * ✅ ENHANCED: Map scroll state and WebView state
 */
fun BrowserTabEntity.toDomain(): BrowserTab {
    return BrowserTab(
        id = id,
        url = url,
        title = title,
        timestamp = timestamp,
        isActive = isActive,
        favicon = BrowserTabEntity.byteArrayToBitmap(faviconBytes),
        screenshot = BrowserTabEntity.byteArrayToBitmap(screenshotBytes),
        // ✅ Map WebView state
        scrollX = scrollX,
        scrollY = scrollY,
        canGoBack = canGoBack,
        canGoForward = canGoForward,
        progress = progress,
        isLoading = isLoading,
        isSecure = isSecure
    )
}

fun BrowserTab.toEntity(): BrowserTabEntity {
    return BrowserTabEntity(
        id = id,
        url = url,
        title = title,
        timestamp = timestamp,
        isActive = isActive,
        faviconBytes = BrowserTabEntity.bitmapToByteArray(favicon),
        screenshotBytes = BrowserTabEntity.bitmapToByteArray(screenshot),
        // ✅ Map WebView state
        scrollX = scrollX,
        scrollY = scrollY,
        canGoBack = canGoBack,
        canGoForward = canGoForward,
        progress = progress,
        isLoading = isLoading,
        isSecure = isSecure
    )
}