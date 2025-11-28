package com.octane.browser.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.octane.browser.data.local.db.entities.ConnectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connections ORDER BY lastUsedAt DESC")
    fun getAllConnections(): Flow<List<ConnectionEntity>>
    
    @Query("SELECT * FROM connections WHERE domain = :domain LIMIT 1")
    fun getConnectionByDomain(domain: String): Flow<ConnectionEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: ConnectionEntity)
    
    @Update
    suspend fun updateConnection(connection: ConnectionEntity)
    
    @Query("DELETE FROM connections WHERE id = :connectionId")
    suspend fun deleteConnection(connectionId: String)
    
    @Query("DELETE FROM connections")
    suspend fun clearAllConnections()
}