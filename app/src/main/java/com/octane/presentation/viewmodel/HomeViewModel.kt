package com.octane.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.core.util.LoadingState
import com.octane.domain.models.Asset
import com.octane.domain.models.Transaction
import com.octane.domain.models.Wallet
import com.octane.domain.usecases.asset.PortfolioState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Home screen orchestrator ViewModel.
 * Delegates to Base ViewModels for shared state management.
 */
class HomeViewModel(
    private val basePortfolio: BasePortfolioViewModel,
    private val baseWallet: BaseWalletViewModel,
    private val baseTransaction: BaseTransactionViewModel
) : ViewModel() {
    
    // ==================== Delegated State ====================
    
    /**
     * Portfolio state (total value, assets, P&L).
     * Observes the active wallet's portfolio.
     */
    val portfolioState: StateFlow<LoadingState<PortfolioState>> = basePortfolio.portfolioState
    
    /**
     * Active wallet details.
     */
    val activeWallet: StateFlow<Wallet?> = baseWallet.activeWallet
    
    /**
     * Recent transactions (last 10 for home screen preview).
     */
    val recentTransactions: StateFlow<LoadingState<List<Transaction>>> = baseTransaction.recentTransactions
    
    /**
     * Pending transaction count (for badge).
     */
    val pendingCount: StateFlow<Int> = baseTransaction.pendingCount
    
    /**
     * Refresh state (pull-to-refresh indicator).
     */
    val isRefreshing: StateFlow<Boolean> = basePortfolio.isRefreshing
    
    /**
     * Network status (for offline banner).
     */
    val networkStatus: StateFlow<NetworkStatus> = basePortfolio.networkStatus
    
    // ==================== One-Time Events ====================
    
    private val _events = MutableSharedFlow<HomeEvent>(replay = 0, extraBufferCapacity = 1)
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()
    
    // ==================== Actions ====================
    
    /**
     * Refresh portfolio data (pull-to-refresh).
     */
    fun onRefresh() {
        basePortfolio.refresh()
    }
    
    /**
     * Toggle asset visibility (hide/show from home screen).
     */
    fun onToggleAssetVisibility(assetId: String, isHidden: Boolean) {
        basePortfolio.toggleAssetVisibility(assetId, isHidden)
    }
    
    /**
     * Navigate to wallet switcher.
     */
    fun onWalletClick() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToWallets)
        }
    }
    
    /**
     * Quick action: Send (opens send screen with token pre-selected).
     */
    fun onQuickSend(tokenSymbol: String) {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToSend(tokenSymbol))
        }
    }
    
    /**
     * Quick action: Receive (opens receive screen).
     */
    fun onQuickReceive() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToReceive)
        }
    }
    
    /**
     * Quick action: Swap (opens swap screen).
     */
    fun onQuickSwap() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToSwap)
        }
    }
    
    /**
     * Quick action: Manage tokens.
     */
    fun onManageTokens() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToManage)
        }
    }
    
    /**
     * Navigate to token details.
     */
    fun onAssetClick(asset: Asset) {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToDetails(asset.id, asset.symbol))
        }
    }
    
    /**
     * Navigate to transaction details.
     */
    fun onTransactionClick(transaction: Transaction) {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToTransactionDetails(transaction.txHash))
        }
    }
    
    // ==================== Formatting Helpers ====================
    
    /**
     * Format currency value based on user preference.
     */
    fun formatCurrency(valueUsd: Double): String {
        return basePortfolio.formatCurrency(valueUsd)
    }
    
    /**
     * Format change percentage with color.
     */
    fun formatChange(changePercent: Double): Pair<androidx.compose.ui.graphics.Color, String> {
        return basePortfolio.formatChange(changePercent)
    }
}

/**
 * One-time navigation events from Home screen.
 */
sealed interface HomeEvent {
    data object NavigateToWallets : HomeEvent
    data class NavigateToSend(val tokenSymbol: String) : HomeEvent
    data object NavigateToReceive : HomeEvent
    data object NavigateToSwap : HomeEvent
    data object NavigateToManage : HomeEvent
    data class NavigateToDetails(val assetId: String, val symbol: String) : HomeEvent
    data class NavigateToTransactionDetails(val txHash: String) : HomeEvent
}