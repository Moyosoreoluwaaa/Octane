package com.octane.browser.domain.repository

import com.octane.browser.domain.models.HistoryEntry
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getAllHistory(): Flow<List<HistoryEntry>>
    fun searchHistory(query: String): Flow<List<HistoryEntry>>
    suspend fun addToHistory(url: String, title: String)
    suspend fun deleteHistoryEntry(entryId: String)
    suspend fun clearAllHistory()
}