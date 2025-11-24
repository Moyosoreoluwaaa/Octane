package com.octane.domain.usecases.discover

import com.octane.core.util.LoadingState
import com.octane.domain.models.Perp
import com.octane.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observe all perps with automatic refresh.
 */
class ObservePerpsUseCase(
    private val repository: DiscoverRepository
) {
    operator fun invoke(): Flow<LoadingState<List<Perp>>> {
        return repository.observePerps()
    }
}