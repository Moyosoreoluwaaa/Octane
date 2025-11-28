package com.octane.browser.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.browser.domain.models.BrowserTab
import com.octane.browser.domain.repository.TabRepository
import com.octane.browser.domain.usecases.tab.CloseTabUseCase
import com.octane.browser.domain.usecases.tab.CreateNewTabUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Tab Manager/Switcher screen
 * Uses shared TabRepository - changes reflect in BrowserViewModel
 */
class TabManagerViewModel(
    private val tabRepository: TabRepository,
    private val createNewTabUseCase: CreateNewTabUseCase,
    private val closeTabUseCase: CloseTabUseCase
) : ViewModel() {

    val tabs: StateFlow<List<BrowserTab>> = tabRepository.getAllTabs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createNewTab() {
        viewModelScope.launch {
            createNewTabUseCase(url = "about:blank", makeActive = true)
        }
    }

    fun closeTab(tabId: String) {
        viewModelScope.launch {
            closeTabUseCase(tabId)
        }
    }

    fun closeAllTabs() {
        viewModelScope.launch {
            tabs.value.forEach { tab ->
                closeTabUseCase(tab.id)
            }
            // Create new tab so we don't end up with zero tabs
            createNewTabUseCase(url = "about:blank", makeActive = true)
        }
    }
}