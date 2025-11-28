package com.octane.browser.domain.models

data class HistoryEntry(
    val id: String,
    val url: String,
    val title: String,
    val visitedAt: Long,
    val visitCount: Int = 1
)