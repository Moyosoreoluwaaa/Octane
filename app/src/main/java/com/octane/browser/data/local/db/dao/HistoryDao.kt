package com.octane.browser.data.local.db.dao

import androidx.room.*
import com.octane.browser.data.local.db.entities.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT 500")
    fun getAllHistory(): Flow<List<HistoryEntity>>
    
    @Query("SELECT * FROM history WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY visitedAt DESC LIMIT 100")
    fun searchHistory(query: String): Flow<List<HistoryEntity>>
    
    @Query("SELECT * FROM history WHERE url = :url LIMIT 1")
    suspend fun getHistoryByUrl(url: String): HistoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)
    
    @Query("DELETE FROM history WHERE id = :entryId")
    suspend fun deleteHistoryEntry(entryId: String)
    
    @Query("DELETE FROM history")
    suspend fun clearAllHistory()
}