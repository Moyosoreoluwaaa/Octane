package com.octane.browser.domain.usecases.tab

import com.octane.browser.domain.repository.TabRepository

class SwitchTabUseCase(
    private val tabRepository: TabRepository
) {
    suspend operator fun invoke(tabId: String) {
        tabRepository.setActiveTab(tabId)
    }
}