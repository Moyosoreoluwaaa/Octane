package com.octane.domain.usecases.discover

import com.octane.core.util.LoadingState
import com.octane.domain.models.Token
import com.octane.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observe all tokens with automatic refresh.
 */
class ObserveTokensUseCase(
    private val repository: DiscoverRepository
) {
    operator fun invoke(): Flow<LoadingState<List<Token>>> {
        return repository.observeTokens()
    }
}
