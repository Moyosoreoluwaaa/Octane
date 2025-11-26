// Create: GetTransactionByHashUseCase.kt
package com.octane.domain.usecases.transaction

import com.octane.domain.models.Transaction
import com.octane.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Get single transaction by hash for detail view.
 * Observes changes to transaction status (pending → confirmed).
 */
class GetTransactionByHashUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(txHash: String): Flow<Transaction?> {
        return transactionRepository.observeTransactionByHash(txHash)
    }
}