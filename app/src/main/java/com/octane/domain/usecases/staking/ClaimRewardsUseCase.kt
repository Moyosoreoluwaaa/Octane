// domain/usecases/staking/ClaimRewardsUseCase.kt

package com.octane.domain.usecases.staking

import com.octane.domain.models.Transaction
import com.octane.domain.repository.StakingRepository
import com.octane.domain.repository.TransactionRepository

/**
 * Claims accumulated staking rewards.
 * On Solana, rewards are auto-compounded, but this can be used
 * to withdraw rewards to the main wallet.
 * 
 * @param positionId Staking position ID
 * @return Result with Transaction or error
 */
class ClaimRewardsUseCase(
    private val stakingRepository: StakingRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(positionId: String): Result<Transaction> {
        return try {
            val transaction = stakingRepository.claimRewards(positionId)
            transactionRepository.insertTransaction(transaction)
            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}