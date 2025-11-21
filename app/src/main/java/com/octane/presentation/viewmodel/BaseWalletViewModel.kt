package com.octane.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.core.util.LoadingState
import com.octane.domain.models.Wallet
import com.octane.domain.usecase.SetActiveWalletUseCase
import com.octane.domain.usecase.wallet.CreateWalletUseCase
import com.octane.domain.usecase.wallet.DeleteWalletUseCase
import com.octane.domain.usecase.wallet.ImportWalletUseCase
import com.octane.domain.usecase.wallet.ObserveWalletsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Shared wallet management state.
 * Used by: WalletsScreen, WalletSwitcherSheet, SettingsScreen
 * 
 * RESPONSIBILITY:
 * - Wallet CRUD (create, import, delete)
 * - Active wallet switching
 * - Wallet list observation
 * 
 * Pattern: shared/BaseWalletViewModel.kt
 */
open class BaseWalletViewModel (
    private val observeWalletsUseCase: ObserveWalletsUseCase,
    private val createWalletUseCase: CreateWalletUseCase,
    private val importWalletUseCase: ImportWalletUseCase,
    private val deleteWalletUseCase: DeleteWalletUseCase,
    private val setActiveWalletUseCase: SetActiveWalletUseCase // You'll need to create this
) : ViewModel() {
    
    // UI State: All wallets
    val walletsState: StateFlow<LoadingState<List<Wallet>>> = observeWalletsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )
    
    // UI State: Active wallet
    val activeWallet: StateFlow<Wallet?> = walletsState
        .map { state ->
            (state as? LoadingState.Success)?.data?.find { it.isActive }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // One-time events (navigation, toasts)
    private val _events = MutableSharedFlow<WalletEvent>(replay = 0, extraBufferCapacity = 1)
    val events: SharedFlow<WalletEvent> = _events.asSharedFlow()
    
    /**
     * Create new wallet with generated keypair.
     * Shows seed phrase screen after creation.
     */
    fun createWallet(
        name: String,
        iconEmoji: String? = null,
        colorHex: String? = null
    ) {
        viewModelScope.launch {
            createWalletUseCase(name, iconEmoji, colorHex)
                .onSuccess { wallet ->
                    _events.emit(WalletEvent.WalletCreated(wallet))
                }
                .onFailure { e ->
                    _events.emit(WalletEvent.Error("Failed to create wallet: ${e.message}"))
                }
        }
    }
    
    /**
     * Import existing wallet from seed phrase.
     * @param seedPhrase 12 or 24-word BIP39 phrase
     * @param name Wallet name
     */
    fun importWallet(
        seedPhrase: String,
        name: String,
        iconEmoji: String? = null,
        colorHex: String? = null
    ) {
        viewModelScope.launch {
            importWalletUseCase(seedPhrase, name, iconEmoji, colorHex)
                .onSuccess { wallet ->
                    _events.emit(WalletEvent.WalletImported(wallet))
                }
                .onFailure { e ->
                    _events.emit(WalletEvent.Error(e.message ?: "Import failed"))
                }
        }
    }
    
    /**
     * Delete wallet permanently.
     * Shows confirmation dialog before deletion.
     * @param walletId Wallet database ID
     */
    fun deleteWallet(walletId: String) {
        viewModelScope.launch {
            deleteWalletUseCase(walletId)
                .onSuccess {
                    _events.emit(WalletEvent.WalletDeleted)
                }
                .onFailure { e ->
                    _events.emit(WalletEvent.Error(e.message ?: "Delete failed"))
                }
        }
    }
    
    /**
     * Switch active wallet.
     * Triggers portfolio refresh, updates all screens.
     * @param walletId Wallet to activate
     */
    fun switchWallet(walletId: String) {
        viewModelScope.launch {
            setActiveWalletUseCase(walletId)
                .onSuccess {
                    _events.emit(WalletEvent.WalletSwitched)
                }
                .onFailure { e ->
                    _events.emit(WalletEvent.Error("Failed to switch wallet"))
                }
        }
    }
    
    /**
     * Update wallet metadata (name, emoji, color).
     */
    fun updateWalletMetadata(
        walletId: String,
        name: String? = null,
        iconEmoji: String? = null,
        colorHex: String? = null
    ) {
        viewModelScope.launch {
            // You'll need UpdateWalletUseCase
            // updateWalletUseCase(walletId, name, iconEmoji, colorHex)
        }
    }
}

/**
 * One-time wallet events for navigation/toasts.
 */
sealed interface WalletEvent {
    data class WalletCreated(val wallet: Wallet) : WalletEvent
    data class WalletImported(val wallet: Wallet) : WalletEvent
    data object WalletDeleted : WalletEvent
    data object WalletSwitched : WalletEvent
    data class Error(val message: String) : WalletEvent
}