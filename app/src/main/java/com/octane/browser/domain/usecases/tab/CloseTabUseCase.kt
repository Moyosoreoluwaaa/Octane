package com.octane.browser.domain.usecases.tab

import com.octane.browser.domain.repository.TabRepository
import kotlinx.coroutines.flow.first

class CloseTabUseCase(
    private val tabRepository: TabRepository
) {
    suspend operator fun invoke(tabId: String) {
        val allTabs = tabRepository.getAllTabs().first()
        val wasActive = allTabs.find { it.id == tabId }?.isActive == true

        tabRepository.deleteTab(tabId)

        // If we closed the active tab, activate another one
        if (wasActive) {
            val remainingTabs = tabRepository.getAllTabs().first()
            if (remainingTabs.isNotEmpty()) {
                tabRepository.setActiveTab(remainingTabs.first().id)
            }
        }
    }
}