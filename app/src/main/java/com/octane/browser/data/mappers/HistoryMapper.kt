package com.octane.browser.data.mappers

import com.octane.browser.data.local.db.entities.HistoryEntity
import com.octane.browser.domain.models.HistoryEntry

fun HistoryEntity.toDomain(): HistoryEntry = HistoryEntry(
    id = id,
    url = url,
    title = title,
    visitedAt = visitedAt,
    visitCount = visitCount
)

fun HistoryEntry.toEntity(): HistoryEntity = HistoryEntity(
    id = id,
    url = url,
    title = title,
    visitedAt = visitedAt,
    visitCount = visitCount
)
