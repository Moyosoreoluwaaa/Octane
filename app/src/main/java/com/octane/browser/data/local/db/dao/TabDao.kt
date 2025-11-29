package com.octane.browser.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.octane.browser.data.local.db.entity.BrowserTabEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TabDao {
    @Query("SELECT * FROM browser_tabs ORDER BY timestamp DESC")
    fun getAllTabs(): Flow<List<BrowserTabEntity>>

    @Query("SELECT * FROM browser_tabs WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveTab(): BrowserTabEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: BrowserTabEntity)

    @Update
    suspend fun updateTab(tab: BrowserTabEntity)

    @Query("DELETE FROM browser_tabs WHERE id = :tabId")
    suspend fun deleteTab(tabId: String)

    @Query("UPDATE browser_tabs SET isActive = 0")
    suspend fun deactivateAllTabs()

    @Query("UPDATE browser_tabs SET isActive = 1 WHERE id = :tabId")
    suspend fun setActiveTab(tabId: String)

    @Query("DELETE FROM browser_tabs")
    suspend fun clearAllTabs()
}