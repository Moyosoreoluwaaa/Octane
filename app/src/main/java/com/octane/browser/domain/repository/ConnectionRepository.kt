package com.octane.browser.domain.repository

import com.octane.browser.domain.models.DAppConnection
import kotlinx.coroutines.flow.Flow

interface ConnectionRepository {
    fun getAllConnections(): Flow<List<DAppConnection>>
    fun getConnectionByDomain(domain: String): Flow<DAppConnection?>
    suspend fun insertConnection(connection: DAppConnection)
    suspend fun updateConnection(connection: DAppConnection)
    suspend fun deleteConnection(connectionId: String)
    suspend fun clearAllConnections()
}