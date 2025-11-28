package com.octane.browser.data.mappers

import com.octane.browser.data.local.db.entities.BookmarkEntity
import com.octane.browser.domain.models.Bookmark

fun BookmarkEntity.toDomain(): Bookmark = Bookmark(
    id = id,
    url = url,
    title = title,
    faviconUrl = faviconUrl,
    folder = folder,
    createdAt = createdAt
)

fun Bookmark.toEntity(): BookmarkEntity = BookmarkEntity(
    id = id,
    url = url,
    title = title,
    faviconUrl = faviconUrl,
    folder = folder,
    createdAt = createdAt
)