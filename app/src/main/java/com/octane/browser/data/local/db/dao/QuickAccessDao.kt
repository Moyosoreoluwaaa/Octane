package com.octane.browser.data.local.db.dao

import QuickAccessEntity
import android.graphics.Bitmap
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * ✅ DAO for Quick Access CRUD operations
 */
@Dao
interface QuickAccessDao {

    @Query("SELECT * FROM quick_access ORDER BY position ASC")
    fun getAllQuickAccess(): Flow<List<QuickAccessEntity>>

    @Query("SELECT * FROM quick_access WHERE id = :id")
    suspend fun getQuickAccessById(id: Long): QuickAccessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuickAccess(quickAccess: QuickAccessEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quickAccessList: List<QuickAccessEntity>)

    @Update
    suspend fun updateQuickAccess(quickAccess: QuickAccessEntity)

    @Query("DELETE FROM quick_access WHERE id = :id")
    suspend fun deleteQuickAccess(id: Long)

    @Query("DELETE FROM quick_access")
    suspend fun deleteAll()

    @Query("UPDATE quick_access SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: Long, position: Int)

    @Query("SELECT COUNT(*) FROM quick_access")
    suspend fun getCount(): Int

    @Query("SELECT MAX(position) FROM quick_access")
    suspend fun getMaxPosition(): Int?

    // ❌ REMOVED: The problematic @Query updateFavicon method.
    // @Query("UPDATE quick_access SET favicon = :favicon WHERE url = :url")
    // suspend fun updateFavicon(url: String, favicon: Bitmap?)

    // ✅ NEW: A function to perform the update using a partial entity wrapper.
    suspend fun updateFaviconByUrl(url: String, favicon: Bitmap?) {
        val existingEntity = getQuickAccessByUrl(url)
        if (existingEntity != null) {
            // Create a copy of the existing entity with the new favicon
            val updatedEntity = existingEntity.copy(favicon = favicon)
            // Use the existing @Update method which correctly applies TypeConverters
            updateQuickAccess(updatedEntity)
        }
    }

    // 💡 Helper query to fetch the entity by URL for the update operation
    @Query("SELECT * FROM quick_access WHERE url = :url LIMIT 1")
    suspend fun getQuickAccessByUrl(url: String): QuickAccessEntity?
}