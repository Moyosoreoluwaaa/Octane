package com.octane.domain.usecases.discover

import com.octane.core.util.LoadingState
import com.octane.domain.repository.DiscoverRepository

/**
 * Manually refresh dApps from API.
 */
class RefreshDAppsUseCase(
    private val repository: DiscoverRepository
) {
    suspend operator fun invoke(): LoadingState<Unit> {
        return repository.refreshDApps()
    }
}