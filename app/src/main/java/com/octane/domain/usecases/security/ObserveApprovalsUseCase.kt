// domain/usecases/security/ObserveApprovalsUseCase.kt

package com.octane.domain.usecases.security

import com.octane.core.util.LoadingState
import com.octane.domain.models.Approval
import com.octane.domain.repository.ApprovalRepository
import com.octane.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Observes all token approvals for the active wallet.
 * Used in Security Dashboard (V1.8) to audit spend allowances.
 */
class ObserveApprovalsUseCase(
    private val approvalRepository: ApprovalRepository,
    private val walletRepository: WalletRepository
) {
    operator fun invoke(): Flow<LoadingState<List<Approval>>> {
        return walletRepository.observeActiveWallet()
            .flatMapLatest { wallet ->
                if (wallet == null) {
                    flowOf(LoadingState.Error(IllegalStateException("No active wallet")))
                } else {
                    approvalRepository.observeApprovals(wallet.id)
                        .map<List<Approval>, LoadingState<List<Approval>>> { approvals ->
                            LoadingState.Success(approvals.filter { !it.isRevoked })
                        }
                }
            }
    }
}