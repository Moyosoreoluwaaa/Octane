package com.octane.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.core.util.LoadingState
import com.octane.domain.models.Asset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Discover/Search Screen ViewModel.
 * Handles token search, trending tokens, and discovery features.
 */
class DiscoverViewModel(
    private val basePortfolio: BasePortfolioViewModel
) : ViewModel() {
    
    // ==================== UI State ====================
    
    /**
     * Search query.
     */
    private val _searchQuery = MutableStateFlow("Sites, tokens, URL")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    /**
     * Selected mode (Tokens, Perps, Lists).
     */
    private val _selectedMode = MutableStateFlow("Tokens")
    val selectedMode: StateFlow<String> = _selectedMode.asStateFlow()
    
    /**
     * Trending tokens (from portfolio or external API).
     */
    private val _trendingTokens = MutableStateFlow<LoadingState<List<Asset>>>(LoadingState.Loading)
    val trendingTokens: StateFlow<LoadingState<List<Asset>>> = _trendingTokens.asStateFlow()
    
    /**
     * Search results.
     */
    private val _searchResults = MutableStateFlow<LoadingState<List<Asset>>>(LoadingState.Loading)
    val searchResults: StateFlow<LoadingState<List<Asset>>> = _searchResults.asStateFlow()
    
    init {
        loadTrendingTokens()
    }
    
    // ==================== Actions ====================
    
    /**
     * Update search query.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank() && query != "Sites, tokens, URL") {
            performSearch(query)
        } else {
            _searchResults.value = LoadingState.Loading
        }
    }
    
    /**
     * Switch mode.
     */
    fun onModeSelected(mode: String) {
        _selectedMode.value = mode
        // Reload content based on mode
        when (mode) {
            "Tokens" -> loadTrendingTokens()
            "Perps" -> loadPerps()
            "Lists" -> loadLists()
        }
    }
    
    /**
     * Clear search.
     */
    fun clearSearch() {
        _searchQuery.value = "Sites, tokens, URL"
        _searchResults.value = LoadingState.Loading
    }
    
    // ==================== Private Methods ====================
    
    private fun loadTrendingTokens() {
        viewModelScope.launch {
            // TODO: Fetch trending tokens from external API
            // For now, use portfolio data
            basePortfolio.portfolioState.collect { state ->
                _trendingTokens.value = when (state) {
                    is LoadingState.Success -> {
                        LoadingState.Success(
                            state.data.assets
                                .sortedByDescending { it.priceChange24h }
                                .take(10)
                        )
                    }
                    is LoadingState.Loading -> LoadingState.Loading
                    is LoadingState.Error -> state as LoadingState<List<Asset>>
                    else -> LoadingState.Loading
                }
            }
        }
    }
    
    private fun performSearch(query: String) {
        viewModelScope.launch {
            // TODO: Implement search across multiple sources
            // For now, search in portfolio
            basePortfolio.portfolioState.collect { state ->
                _searchResults.value = when (state) {
                    is LoadingState.Success -> {
                        LoadingState.Success(
                            state.data.assets.filter {
                                it.name.contains(query, ignoreCase = true) ||
                                it.symbol.contains(query, ignoreCase = true)
                            }
                        )
                    }
                    else -> state as LoadingState<List<Asset>>
                }
            }
        }
    }
    
    private fun loadPerps() {
        // TODO: Load perpetual futures data
    }
    
    private fun loadLists() {
        // TODO: Load curated lists (trending sites, learn content)
    }
}