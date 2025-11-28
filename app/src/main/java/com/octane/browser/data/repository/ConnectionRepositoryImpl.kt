package com.octane.browser.data.repository

import com.octane.browser.data.local.db.dao.ConnectionDao
import com.octane.browser.data.mappers.toDomain
import com.octane.browser.data.mappers.toEntity
import com.octane.browser.domain.models.DAppConnection
import com.octane.browser.domain.repository.ConnectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ConnectionRepositoryImpl(
    private val connectionDao: ConnectionDao
) : ConnectionRepository {
    
    override fun getAllConnections(): Flow<List<DAppConnection>> {
        return connectionDao.getAllConnections().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getConnectionByDomain(domain: String): Flow<DAppConnection?> {
        return connectionDao.getConnectionByDomain(domain).map { entity ->
            entity?.toDomain()
        }
    }
    
    override suspend fun insertConnection(connection: DAppConnection) {
        connectionDao.insertConnection(connection.toEntity())
    }
    
    override suspend fun updateConnection(connection: DAppConnection) {
        connectionDao.updateConnection(connection.toEntity())
    }
    
    override suspend fun deleteConnection(connectionId: String) {
        connectionDao.deleteConnection(connectionId)
    }
    
    override suspend fun clearAllConnections() {
        connectionDao.clearAllConnections()
    }
}