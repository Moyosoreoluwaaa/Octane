package com.octane.domain.usecases.discover

import com.octane.core.util.LoadingState
import com.octane.domain.models.Token
import com.octane.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * Search tokens by name or symbol.
 */
class SearchTokensUseCase(
    private val repository: DiscoverRepository
) {
    operator fun invoke(query: String): Flow<LoadingState<List<Token>>> {
        return repository.searchTokens(query)
    }
}