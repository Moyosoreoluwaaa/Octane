package com.octane.domain.usecases.wallet

import com.octane.domain.models.Wallet
import com.octane.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe the currently active wallet.
 * Used by DAppBrowserViewModel to sign transactions and manage connections.
 */
class ObserveActiveWalletUseCase(
    private val walletRepository: WalletRepository
) {
    operator fun invoke(): Flow<Wallet?> {
        return walletRepository.observeActiveWallet()
    }
}