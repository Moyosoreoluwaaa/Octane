package com.octane.browser.domain.usecases.connection

import com.octane.browser.domain.models.DAppConnection
import com.octane.browser.domain.repository.ConnectionRepository
import kotlinx.coroutines.flow.Flow

class GetConnectionUseCase(
    private val connectionRepository: ConnectionRepository
) {
    operator fun invoke(domain: String): Flow<DAppConnection?> {
        return connectionRepository.getConnectionByDomain(domain)
    }
}