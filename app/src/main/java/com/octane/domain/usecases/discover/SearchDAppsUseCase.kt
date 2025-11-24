package com.octane.domain.usecases.discover;

import com.octane.core.util.LoadingState
import com.octane.domain.models.DApp
import com.octane.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow
import java.util.List

/**
 * Search dApps by name or description.
 */
class SearchDAppsUseCase(
    val repository:DiscoverRepository
) {
    operator fun invoke(query: String): Flow<LoadingState<kotlin.collections.List<DApp>>>

    {
        return repository.searchDApps(query)
    }
}
