package com.octane.browser.data.mappers

import com.octane.browser.data.local.db.entity.BrowserTabEntity
import com.octane.browser.domain.models.BrowserTab

/**
 * ✅ ENHANCED: Screenshot mapping
 */
fun BrowserTabEntity.toDomain(): BrowserTab {
    return BrowserTab(
        id = id,
        url = url,
        title = title,
        timestamp = timestamp,
        isActive = isActive,
        favicon = BrowserTabEntity.byteArrayToBitmap(faviconBytes),
        screenshot = BrowserTabEntity.byteArrayToBitmap(screenshotBytes) // ✅ NEW
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
        screenshotBytes = BrowserTabEntity.bitmapToByteArray(screenshot) // ✅ NEW
    )
}