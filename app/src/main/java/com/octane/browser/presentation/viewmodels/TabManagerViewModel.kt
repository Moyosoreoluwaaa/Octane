package com.octane.browser.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.browser.domain.models.BrowserTab
import com.octane.browser.domain.repository.SettingsRepository
import com.octane.browser.domain.repository.TabRepository
import com.octane.browser.domain.usecases.tab.CloseTabUseCase
import com.octane.browser.domain.usecases.tab.CreateNewTabUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TabManagerViewModel(
    private val tabRepository: TabRepository,
    private val settingsRepository: SettingsRepository, // Injected
    private val createNewTabUseCase: CreateNewTabUseCase,
    private val closeTabUseCase: CloseTabUseCase
) : ViewModel() {

    val tabs: StateFlow<List<BrowserTab>> = tabRepository.getAllTabs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ✅ PERSISTENT STATE: Observing from DataStore
    val isGridLayout: StateFlow<Boolean> = settingsRepository.observeTabLayout()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun toggleLayout() {
        viewModelScope.launch {
            // Invert current state and save to DataStore
            settingsRepository.updateTabLayout(!isGridLayout.value)
        }
    }

    fun createNewTab() {
        viewModelScope.launch {
            // Create a tab with an empty URL to signal it's a fresh, unused tab
            createNewTabUseCase(url = "", makeActive = true)
        }
    }

    fun closeTab(tabId: String) {
        viewModelScope.launch {
            closeTabUseCase(tabId)
        }
    }
}