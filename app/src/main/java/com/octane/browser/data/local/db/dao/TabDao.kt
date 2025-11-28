package com.octane.browser.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.octane.browser.data.local.db.entities.TabEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TabDao {
    @Query("SELECT * FROM tabs ORDER BY timestamp DESC")
    fun getAllTabs(): Flow<List<TabEntity>>

    @Query("SELECT * FROM tabs WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveTab(): TabEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: TabEntity)

    @Update
    suspend fun updateTab(tab: TabEntity)

    @Query("DELETE FROM tabs WHERE id = :tabId")
    suspend fun deleteTab(tabId: String)

    @Query("UPDATE tabs SET isActive = 0")
    suspend fun deactivateAllTabs()

    @Query("UPDATE tabs SET isActive = 1 WHERE id = :tabId")
    suspend fun setActiveTab(tabId: String)

    @Query("DELETE FROM tabs")
    suspend fun clearAllTabs()
}