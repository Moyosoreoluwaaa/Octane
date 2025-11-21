package com.octane.domain.usecase.wallet

//import com.octane.core.util.LoadingState
import com.octane.core.util.LoadingState
import com.octane.domain.models.Wallet
import com.octane.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Observes all wallets reactively.
 *
 * Returns:
 * - Loading state during initial fetch
 * - Success with list of wallets
 * - Error if database access fails
 */
class ObserveWalletsUseCase(
    private val walletRepository: WalletRepository
) {
    operator fun invoke(): Flow<LoadingState<List<Wallet>>> {
        return walletRepository.observeAllWallets()
            .map<List<Wallet>, LoadingState<List<Wallet>>> { wallets ->
                LoadingState.Success(wallets)
            }
            .catch { e ->
                emit(LoadingState.Error(e) as LoadingState<List<Wallet>>)
            }
    }
}
