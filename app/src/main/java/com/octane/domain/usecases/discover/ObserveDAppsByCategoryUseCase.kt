package com.octane.domain.usecases.discover

import com.octane.core.util.LoadingState
import com.octane.domain.models.DApp
import com.octane.domain.models.DAppCategory
import com.octane.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observe dApps filtered by category.
 */
class ObserveDAppsByCategoryUseCase(
    private val repository: DiscoverRepository
) {
    operator fun invoke(category: DAppCategory): Flow<LoadingState<List<DApp>>> {
        return repository.observeDAppsByCategory(category)
    }
}