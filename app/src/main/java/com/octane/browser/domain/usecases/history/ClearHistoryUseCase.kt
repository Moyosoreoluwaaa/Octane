package com.octane.browser.domain.usecases.history

import com.octane.browser.domain.repository.HistoryRepository


class ClearHistoryUseCase(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke() {
        historyRepository.clearAllHistory()
    }
}
