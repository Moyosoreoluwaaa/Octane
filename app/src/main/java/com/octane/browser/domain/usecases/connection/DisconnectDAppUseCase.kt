package com.octane.browser.domain.usecases.connection

import com.octane.browser.domain.repository.ConnectionRepository

class DisconnectDAppUseCase(
    private val connectionRepository: ConnectionRepository
) {
    suspend operator fun invoke(connectionId: String) {
        connectionRepository.deleteConnection(connectionId)
    }
}