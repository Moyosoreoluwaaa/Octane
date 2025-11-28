package com.octane.browser.domain.usecases.history

import com.octane.browser.domain.repository.HistoryRepository


class RecordVisitUseCase(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(url: String, title: String) {
        // Skip internal URLs
        if (url.startsWith("about:") || url.startsWith("data:")) {
            return
        }
        
        historyRepository.addToHistory(url, title)
    }
}