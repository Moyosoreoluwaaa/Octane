package com.octane.presentation.viewmodel

import android.util.Log // Added import for Android Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.core.util.LoadingState
import com.octane.domain.models.DApp
import com.octane.domain.models.DAppCategory
import com.octane.domain.models.Perp
import com.octane.domain.models.Token
import com.octane.domain.usecases.discover.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
// Removed: import timber.log.Timber

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class DiscoverViewModel(
    private val observeTrendingTokensUseCase: ObserveTrendingTokensUseCase,
    private val searchTokensUseCase: SearchTokensUseCase,
    private val refreshTokensUseCase: RefreshTokensUseCase,
    private val observePerpsUseCase: ObservePerpsUseCase,
    private val searchPerpsUseCase: SearchPerpsUseCase,
    private val refreshPerpsUseCase: RefreshPerpsUseCase,
    private val observeDAppsUseCase: ObserveDAppsUseCase,
    private val searchDAppsUseCase: SearchDAppsUseCase,
    private val refreshDAppsUseCase: RefreshDAppsUseCase
) : ViewModel() {

    private val TAG = "DiscoverViewModel"

    init {
        Log.d(TAG, "🎬 DiscoverViewModel initialized")
        Log.d(TAG, "🎬 Use cases injected successfully")
    }

    // ==================== UI State ====================

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMode = MutableStateFlow<DiscoverMode>(DiscoverMode.TOKENS)
    val selectedMode: StateFlow<DiscoverMode> = _selectedMode.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // ==================== TOKENS ====================

    val trendingTokens: StateFlow<LoadingState<List<Token>>> = observeTrendingTokensUseCase()
        .onEach { state ->
            when (state) {
                is LoadingState.Loading -> Log.d(TAG, "🔵 ViewModel: Trending tokens - Loading")
                is LoadingState.Success -> {
                    Log.i(TAG, "✅ ViewModel: Trending tokens - Success with ${state.data.size} items")
                    if (state.data.isNotEmpty()) {
                        Log.d(TAG, "📋 Top 3 tokens: ${state.data.take(3).map { "${it.symbol}=${it.formattedPrice}" }}")
                    }
                }
                is LoadingState.Error -> Log.e(TAG, "❌ ViewModel: Trending tokens - Error: ${state.message}")
                else -> Log.d(TAG, "⚪ ViewModel: Trending tokens - Idle/Unknown state")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    val tokenSearchResults: StateFlow<LoadingState<List<Token>>> = _searchQuery
        .onEach { query ->
            Log.d(TAG, "🔍 Search query changed: '$query'")
        }
        .debounce(300)
        .onEach { query ->
            Log.d(TAG, "⏰ Debounced search query: '$query'")
        }
        .distinctUntilChanged()
        .filter { it.isNotBlank() }
        .onEach { query ->
            Log.d(TAG, "🔎 Executing search for: '$query'")
        }
        .flatMapLatest { query ->
            searchTokensUseCase(query)
                .onEach { state ->
                    when (state) {
                        is LoadingState.Success -> {
                            Log.d(TAG, "✅ Search results: ${state.data.size} tokens found")
                        }
                        is LoadingState.Error -> {
                            Log.e(TAG, "❌ Search error: ${state.message}")
                        }
                        else -> Log.d(TAG, "🔵 Search loading...")
                    }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    // ==================== PERPS ====================

    val perps: StateFlow<LoadingState<List<Perp>>> = observePerpsUseCase()
        .onEach { state ->
            when (state) {
                is LoadingState.Loading -> Log.d(TAG, "🔵 ViewModel: Perps - Loading")
                is LoadingState.Success -> Log.i(TAG, "✅ ViewModel: Perps - Success with ${state.data.size} items")
                is LoadingState.Error -> Log.e(TAG, "❌ ViewModel: Perps - Error: ${state.message}")
                else -> {}
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    val perpSearchResults: StateFlow<LoadingState<List<Perp>>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .filter { it.isNotBlank() }
        .flatMapLatest { query ->
            Log.d(TAG, "🔎 Searching perps for: '$query'")
            searchPerpsUseCase(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    // ==================== DAPPS ====================

    val dapps: StateFlow<LoadingState<List<DApp>>> = observeDAppsUseCase()
        .onEach { state ->
            when (state) {
                is LoadingState.Loading -> Log.d(TAG, "🔵 ViewModel: DApps - Loading")
                is LoadingState.Success -> Log.i(TAG, "✅ ViewModel: DApps - Success with ${state.data.size} items")
                is LoadingState.Error -> Log.e(TAG, "❌ ViewModel: DApps - Error: ${state.message}")
                else -> {}
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    val dappSearchResults: StateFlow<LoadingState<List<DApp>>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .filter { it.isNotBlank() }
        .flatMapLatest { query ->
            Log.d(TAG, "🔎 Searching dApps for: '$query'")
            searchDAppsUseCase(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    // ==================== Actions ====================

    fun onSearchQueryChanged(query: String) {
        Log.d(TAG, "📝 onSearchQueryChanged: '$query'")
        _searchQuery.value = query
    }

    fun onModeSelected(mode: DiscoverMode) {
        Log.i(TAG, "🎯 Mode selected: $mode (previous: ${_selectedMode.value})")
        _selectedMode.value = mode

        when (mode) {
            DiscoverMode.TOKENS -> {
                Log.d(TAG, "🪙 Switched to Tokens mode, triggering refresh")
                refreshTokens()
            }
            DiscoverMode.PERPS -> {
                Log.d(TAG, "📈 Switched to Perps mode, triggering refresh")
                refreshPerps()
            }
            DiscoverMode.LISTS -> {
                Log.d(TAG, "📱 Switched to Lists mode, triggering refresh")
                refreshDApps()
            }
        }
    }

    fun clearSearch() {
        Log.d(TAG, "🗑️ Clearing search query")
        _searchQuery.value = ""
    }

    fun refresh() {
        if (_isRefreshing.value) {
            Log.w(TAG, "⚠️ Refresh already in progress, ignoring")
            return
        }

        Log.i(TAG, "🔄 Manual refresh triggered for mode: ${_selectedMode.value}")

        when (_selectedMode.value) {
            DiscoverMode.TOKENS -> refreshTokens()
            DiscoverMode.PERPS -> refreshPerps()
            DiscoverMode.LISTS -> refreshDApps()
        }
    }

    private fun refreshTokens() {
        Log.d(TAG, "🔄 refreshTokens() called")

        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                Log.d(TAG, "🔄 Starting token refresh...")

                val result = refreshTokensUseCase()

                when (result) {
                    is LoadingState.Success -> {
                        Log.i(TAG, "✅ Token refresh completed successfully")
                    }
                    is LoadingState.Error -> {
                        Log.e(TAG, "❌ Token refresh failed: ${result.message}")
                    }
                    else -> {
                        Log.w(TAG, "⚠️ Token refresh returned unexpected state: $result")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception during token refresh", e)
            } finally {
                _isRefreshing.value = false
                Log.d(TAG, "🔄 Token refresh completed, isRefreshing set to false")
            }
        }
    }

    private fun refreshPerps() {
        Log.d(TAG, "🔄 refreshPerps() called")

        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                Log.d(TAG, "🔄 Starting perps refresh...")

                refreshPerpsUseCase()

                Log.i(TAG, "✅ Perps refresh completed")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception during perps refresh", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun refreshDApps() {
        Log.d(TAG, "🔄 refreshDApps() called")

        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                Log.d(TAG, "🔄 Starting dApps refresh...")

                refreshDAppsUseCase()

                Log.i(TAG, "✅ dApps refresh completed")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception during dApps refresh", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onTokenClicked(token: Token) {
        Log.d(TAG, "🎯 Token clicked: ${token.symbol} (${token.name})")
        // TODO: Navigate to token detail screen
    }

    fun onPerpClicked(perp: Perp) {
        Log.d(TAG, "🎯 Perp clicked: ${perp.symbol}")
        // TODO: Navigate to perp trading screen
    }

    fun onDAppClicked(dapp: DApp) {
        Log.d(TAG, "🎯 DApp clicked: ${dapp.name}")
        // TODO: Open dApp URL in browser or WebView
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 DiscoverViewModel cleared")
    }
}

enum class DiscoverMode {
    TOKENS,
    PERPS,
    LISTS
}