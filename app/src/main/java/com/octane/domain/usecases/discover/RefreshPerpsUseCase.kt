package com.octane.domain.usecases.discover

import com.octane.core.util.LoadingState
import com.octane.domain.repository.DiscoverRepository

/**
 * Manually refresh perps from API.
 */
class RefreshPerpsUseCase(
    private val repository: DiscoverRepository
) {
    suspend operator fun invoke(): LoadingState<Unit> {
        return repository.refreshPerps()
    }
}
