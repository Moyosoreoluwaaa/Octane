package com.octane.browser.domain.usecases.tab

import com.octane.browser.domain.models.BrowserTab
import com.octane.browser.domain.repository.TabRepository

class GetActiveTabUseCase(
    private val tabRepository: TabRepository
) {
    suspend operator fun invoke(): BrowserTab? {
        return tabRepository.getActiveTab()
    }
}