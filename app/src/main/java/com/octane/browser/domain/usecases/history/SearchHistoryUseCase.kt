package com.octane.browser.domain.usecases.history

import com.octane.browser.domain.repository.HistoryRepository
import com.octane.browser.domain.models.HistoryEntry
import kotlinx.coroutines.flow.Flow


class SearchHistoryUseCase(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(query: String): Flow<List<HistoryEntry>> {
        return if (query.isBlank()) {
            historyRepository.getAllHistory()
        } else {
            historyRepository.searchHistory(query)
        }
    }
}