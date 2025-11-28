package com.octane.browser.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.browser.domain.models.HistoryEntry
import com.octane.browser.domain.usecases.history.ClearHistoryUseCase
import com.octane.browser.domain.usecases.history.SearchHistoryUseCase
import com.octane.browser.domain.repository.HistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for History screen
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val historyRepository: HistoryRepository,
    private val searchHistoryUseCase: SearchHistoryUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val historyEntries: StateFlow<List<HistoryEntry>> = _searchQuery
        .flatMapLatest { query ->
            searchHistoryUseCase(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteHistoryEntry(entryId: String) {
        viewModelScope.launch {
            historyRepository.deleteHistoryEntry(entryId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            clearHistoryUseCase()
        }
    }
}
