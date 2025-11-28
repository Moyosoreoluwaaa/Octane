package com.octane.browser.data.mappers

import com.octane.browser.data.local.db.entities.TabEntity
import com.octane.browser.domain.models.BrowserTab

fun TabEntity.toDomain(): BrowserTab = BrowserTab(
    id = id,
    url = url,
    title = title,
    favicon = null, // Bitmaps not stored in DB
    timestamp = timestamp,
    isActive = isActive
)

fun BrowserTab.toEntity(): TabEntity = TabEntity(
    id = id,
    url = url,
    title = title,
    timestamp = timestamp,
    isActive = isActive
)