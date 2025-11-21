package com.octane.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.octane.core.util.LoadingState
import com.octane.domain.models.Wallet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Wallets screen ViewModel.
 * Extends BaseWalletViewModel with UI-specific state.
 * 
 * RESPONSIBILITY:
 * - Display wallet list with balances
 * - Create/import/delete flows
 * - Wallet switcher UI state
 * - Edit wallet metadata (name, emoji, color)
 * 
 * Pattern: features/wallets/WalletsViewModel.kt
 */
class WalletsViewModel(
    private val baseWallet: BaseWalletViewModel
) : ViewModel() {
    
    // Delegate: Wallet list
    val walletsState = baseWallet.walletsState
    
    // Delegate: Active wallet
    val activeWallet = baseWallet.activeWallet
    
    // Delegate: Wallet events
    val walletEvents = baseWallet.events
    
    // UI State: Bottom sheets
    private val _uiState = MutableStateFlow(WalletsUiState())
    val uiState: StateFlow<WalletsUiState> = _uiState.asStateFlow()
    
    /**
     * Show create wallet sheet.
     */
    fun showCreateWallet() {
        _uiState.update { it.copy(showCreateSheet = true) }
    }
    
    fun hideCreateWallet() {
        _uiState.update { it.copy(showCreateSheet = false) }
    }
    
    /**
     * Show import wallet sheet.
     */
    fun showImportWallet() {
        _uiState.update { it.copy(showImportSheet = true) }
    }
    
    fun hideImportWallet() {
        _uiState.update { it.copy(showImportSheet = false) }
    }
    
    /**
     * Show delete confirmation dialog.
     */
    fun showDeleteConfirmation(walletId: String) {
        _uiState.update { 
            it.copy(
                showDeleteConfirmation = true,
                walletToDelete = walletId
            ) 
        }
    }
    
    fun hideDeleteConfirmation() {
        _uiState.update { 
            it.copy(
                showDeleteConfirmation = false,
                walletToDelete = null
            ) 
        }
    }
    
    /**
     * Show edit wallet sheet.
     */
    fun showEditWallet(walletId: String) {
        val wallet = (walletsState.value as? LoadingState.Success)
            ?.data?.find { it.id == walletId }
        
        if (wallet != null) {
            _uiState.update {
                it.copy(
                    showEditSheet = true,
                    editingWallet = wallet
                )
            }
        }
    }
    
    fun hideEditWallet() {
        _uiState.update {
            it.copy(
                showEditSheet = false,
                editingWallet = null
            )
        }
    }
    
    /**
     * Create new wallet (delegates to base).
     */
    fun createWallet(
        name: String,
        iconEmoji: String?,
        colorHex: String?
    ) {
        baseWallet.createWallet(name, iconEmoji, colorHex)
        hideCreateWallet()
    }
    
    /**
     * Import existing wallet (delegates to base).
     */
    fun importWallet(
        seedPhrase: String,
        name: String,
        iconEmoji: String?,
        colorHex: String?
    ) {
        baseWallet.importWallet(seedPhrase, name, iconEmoji, colorHex)
        hideImportWallet()
    }
    
    /**
     * Delete wallet with confirmation.
     */
    fun deleteWallet() {
        val walletId = _uiState.value.walletToDelete
        if (walletId != null) {
            baseWallet.deleteWallet(walletId)
            hideDeleteConfirmation()
        }
    }
    
    /**
     * Switch active wallet (delegates to base).
     */
    fun switchWallet(walletId: String) {
        baseWallet.switchWallet(walletId)
    }
    
    /**
     * Update wallet metadata.
     */
    fun updateWallet(
        walletId: String,
        name: String?,
        iconEmoji: String?,
        colorHex: String?
    ) {
        baseWallet.updateWalletMetadata(walletId, name, iconEmoji, colorHex)
        hideEditWallet()
    }
    
    /**
     * Get wallet display name with fallback.
     */
    fun getWalletDisplayName(wallet: Wallet): String {
        return wallet.name.ifBlank { "Wallet ${wallet.id.take(4)}" }
    }
    
    /**
     * Get wallet color as Compose Color.
     */
    fun getWalletColor(wallet: Wallet): Color {
        return try {
            Color(android.graphics.Color.parseColor(wallet.colorHex))
        } catch (e: Exception) {
            Color(0xFF4ECDC4) // Default teal
        }
    }
}

/**
 * Wallets UI state (sheets, dialogs).
 */
data class WalletsUiState(
    val showCreateSheet: Boolean = false,
    val showImportSheet: Boolean = false,
    val showEditSheet: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val walletToDelete: String? = null,
    val editingWallet: Wallet? = null
)