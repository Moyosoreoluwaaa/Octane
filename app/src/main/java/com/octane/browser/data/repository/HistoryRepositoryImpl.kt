package com.octane.browser.data.repository

import com.octane.browser.data.local.db.dao.HistoryDao
import com.octane.browser.data.mappers.toDomain
import com.octane.browser.data.mappers.toEntity
import com.octane.browser.domain.models.HistoryEntry
import com.octane.browser.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class HistoryRepositoryImpl(
    private val historyDao: HistoryDao
) : HistoryRepository {
    
    override fun getAllHistory(): Flow<List<HistoryEntry>> {
        return historyDao.getAllHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun searchHistory(query: String): Flow<List<HistoryEntry>> {
        return historyDao.searchHistory(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun addToHistory(url: String, title: String) {
        val existing = historyDao.getHistoryByUrl(url)
        if (existing != null) {
            // Update existing entry
            val updated = existing.copy(
                visitedAt = System.currentTimeMillis(),
                visitCount = existing.visitCount + 1
            )
            historyDao.insertHistory(updated)
        } else {
            // Create new entry
            val newEntry = HistoryEntry(
                id = UUID.randomUUID().toString(),
                url = url,
                title = title,
                visitedAt = System.currentTimeMillis(),
                visitCount = 1
            )
            historyDao.insertHistory(newEntry.toEntity())
        }
    }
    
    override suspend fun deleteHistoryEntry(entryId: String) {
        historyDao.deleteHistoryEntry(entryId)
    }
    
    override suspend fun clearAllHistory() {
        historyDao.clearAllHistory()
    }
}
