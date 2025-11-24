package com.octane.domain.usecases.discover

import com.octane.core.util.LoadingState
import com.octane.domain.models.Perp
import com.octane.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * Search perps by symbol.
 */
class SearchPerpsUseCase(
    private val repository: DiscoverRepository
) {
    operator fun invoke(query: String): Flow<LoadingState<List<Perp>>> {
        return repository.searchPerps(query)
    }
}