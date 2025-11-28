package com.octane.browser.domain.usecases.connection

import com.octane.browser.domain.models.DAppConnection
import com.octane.browser.domain.repository.ConnectionRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

class RequestConnectionUseCase(
    private val connectionRepository: ConnectionRepository
) {
    suspend operator fun invoke(
        domain: String,
        chainId: String,
        connectedAddress: String,
        permissions: List<String> = emptyList()
    ): Result<DAppConnection> {
        // Check if already connected
        val existing = connectionRepository.getConnectionByDomain(domain).first()
        
        if (existing != null) {
            // Update last used time
            val updated = existing.copy(
                lastUsedAt = System.currentTimeMillis(),
                chainId = chainId,
                connectedAddress = connectedAddress
            )
            connectionRepository.updateConnection(updated)
            return Result.success(updated)
        }
        
        // Create new connection
        val connection = DAppConnection(
            id = UUID.randomUUID().toString(),
            domain = domain,
            chainId = chainId,
            connectedAddress = connectedAddress,
            connectedAt = System.currentTimeMillis(),
            lastUsedAt = System.currentTimeMillis(),
            permissions = permissions
        )
        
        connectionRepository.insertConnection(connection)
        return Result.success(connection)
    }
}
