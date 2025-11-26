package com.octane.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.core.util.LoadingState
import com.octane.domain.models.Wallet
import com.octane.domain.usecases.wallet.CreateWalletUseCase
import com.octane.domain.usecases.wallet.DeleteWalletUseCase
import com.octane.domain.usecases.wallet.ImportWalletUseCase
import com.octane.domain.usecases.wallet.ObserveWalletsUseCase
import com.octane.domain.usecases.wallet.SetActiveWalletUseCase
import com.octane.domain.usecases.wallet.UpdateWalletUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class BaseWalletViewModel(
    private val observeWalletsUseCase: ObserveWalletsUseCase,
    private val createWalletUseCase: CreateWalletUseCase,
    private val importWalletUseCase: ImportWalletUseCase,
    private val deleteWalletUseCase: DeleteWalletUseCase,
    private val setActiveWalletUseCase: SetActiveWalletUseCase,
    private val updateWalletUseCase: UpdateWalletUseCase
) : ViewModel() {

    val walletsState: StateFlow<LoadingState<List<Wallet>>> = observeWalletsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    val activeWallet: StateFlow<Wallet?> = walletsState
        .map { state ->
            (state as? LoadingState.Success)?.data?.find { it.isActive }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _events = MutableSharedFlow<WalletEvent>(replay = 0, extraBufferCapacity = 1)
    val events: SharedFlow<WalletEvent> = _events.asSharedFlow()

    fun createWallet(
        name: String,
        iconEmoji: String? = null,
        colorHex: String? = null
    ) {
        viewModelScope.launch {
            createWalletUseCase(name, iconEmoji, colorHex)
                .onSuccess { result ->
                    _events.emit(
                        WalletEvent.WalletCreatedWithSeed(
                            wallet = result.wallet,
                            seedPhrase = result.seedPhrase,
                            iconEmoji = iconEmoji,
                        )
                    )
                }
                .onFailure { e ->
                    _events.emit(WalletEvent.Error("Failed to create wallet: ${e.message}"))
                }
        }
    }

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

    fun updateWalletMetadata(
        walletId: String,
        name: String? = null,
        iconEmoji: String? = null,
        colorHex: String? = null
    ) {
        viewModelScope.launch {
            updateWalletUseCase(walletId, name, iconEmoji, colorHex)
                .onSuccess {
                    _events.emit(WalletEvent.WalletUpdated)
                }
                .onFailure { e ->
                    _events.emit(WalletEvent.Error("Failed to update wallet"))
                }
        }
    }
}

sealed interface WalletEvent {
    data class WalletCreated(val wallet: Wallet) : WalletEvent
    data class WalletCreatedWithSeed(
        val wallet: Wallet,
        val seedPhrase: String,
        val iconEmoji: String?
    ) : WalletEvent

    data class WalletImported(val wallet: Wallet) : WalletEvent
    data object WalletDeleted : WalletEvent
    data object WalletSwitched : WalletEvent
    data object WalletUpdated : WalletEvent
    data class Error(val message: String) : WalletEvent
}