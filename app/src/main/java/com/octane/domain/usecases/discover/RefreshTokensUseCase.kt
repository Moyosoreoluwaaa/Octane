package com.octane.domain.usecases.discover

import com.octane.core.util.LoadingState
import com.octane.domain.repository.DiscoverRepository

/**
 * Manually refresh tokens from API.
 */
class RefreshTokensUseCase(
    private val repository: DiscoverRepository
) {
    suspend operator fun invoke(): LoadingState<Unit> {
        return repository.refreshTokens()
    }
}