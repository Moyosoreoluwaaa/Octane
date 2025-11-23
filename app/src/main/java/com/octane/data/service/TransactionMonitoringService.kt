//package com.octane.data.service
//
//import com.octane.data.local.database.dao.TransactionDao
//import com.octane.data.remote.api.SolanaRpcApi
//import com.octane.data.remote.dto.solana.RpcRequest
//import com.octane.domain.models.TransactionStatus
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.isActive
//import kotlinx.coroutines.launch
//import kotlin.time.Duration.Companion.seconds
//
///**
// * Real-time transaction monitoring service.
// * Polls Solana blockchain for pending transaction status updates.
// */
//class TransactionMonitoringService(
//    private val transactionDao: TransactionDao,
//    private val solanaRpcApi: SolanaRpcApi
//) {
//    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
//    private var isMonitoring = false
//
//    /**
//     * Start monitoring pending transactions.
//     * Checks every 5 seconds for status updates.
//     */
//    fun startMonitoring() {
//        if (isMonitoring) return
//        isMonitoring = true
//
//        scope.launch {
//            while (isActive && isMonitoring) {
//                monitorPendingTransactions()
//                delay(5.seconds)
//            }
//        }
//    }
//
//    fun stopMonitoring() {
//        isMonitoring = false
//    }
//
//    /**
//     * REAL IMPLEMENTATION: Monitor pending transactions on Solana.
//     */
//    private suspend fun monitorPendingTransactions() {
//        try {
//            // Get all pending transactions from local DB
//            val pendingTxs = transactionDao.observePendingTransactions()
//                .first()
//
//            if (pendingTxs.isEmpty()) return
//
//            // Batch check signatures (max 256 at a time per Solana RPC limits)
//            pendingTxs.chunked(256).forEach { batch ->
//                checkTransactionStatuses(batch.map { it.txHash })
//            }
//        } catch (e: Exception) {
//            // Log error but continue monitoring
//            println("Transaction monitoring error: ${e.message}")
//        }
//    }
//
//    /**
//     * Check transaction statuses on Solana blockchain.
//     */
//    private suspend fun checkTransactionStatuses(signatures: List<String>) {
//        try {
//            val request = RpcRequest(
//                method = "getSignatureStatuses",
//                params = listOf(signatures)
//            )
//
//            val response = solanaRpcApi.getSignatureStatuses(request)
//
//            if (response.error != null) {
//                println("RPC error: ${response.error.message}")
//                return
//            }
//
//            response.result?.value?.forEachIndexed { index, statusInfo ->
//                val signature = signatures[index]
//
//                if (statusInfo == null) {
//                    // Transaction not found yet - still pending
//                    return@forEachIndexed
//                }
//
//                // Update transaction status based on confirmations
//                val newStatus = when {
//                    statusInfo.err != null -> TransactionStatus.FAILED
//                    (statusInfo.confirmations ?: 0) >= 32 -> TransactionStatus.CONFIRMED
//                    else -> TransactionStatus.PENDING
//                }
//
//                // Update in database
//                when (newStatus) {
//                    TransactionStatus.CONFIRMED -> {
//                        transactionDao.updateTransactionStatus(
//                            txHash = signature,
//                            status = "CONFIRMED",
//                            confirmationCount = statusInfo.confirmations ?: 32,
//                            blockNumber = statusInfo.slot
//                        )
//                    }
//
//                    TransactionStatus.FAILED -> {
//                        transactionDao.updateTransactionStatus(
//                            txHash = signature,
//                            status = "FAILED",
//                            confirmationCount = 0,
//                            blockNumber = statusInfo.slot,
//                            errorMessage = statusInfo.err?.toString()
//                        )
//                    }
//
//                    TransactionStatus.PENDING -> {
//                        // Still pending - update confirmation count
//                        transactionDao.updateConfirmationCount(
//                            txHash = signature,
//                            confirmationCount = statusInfo.confirmations ?: 0
//                        )
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            println("Status check error: ${e.message}")
//        }
//    }
//
//    /**
//     * Force check a specific transaction.
//     */
//    suspend fun checkTransaction(signature: String): TransactionStatus {
//        return try {
//            val request = RpcRequest(
//                method = "getSignatureStatuses",
//                params = listOf(listOf(signature))
//            )
//
//            val response = solanaRpcApi.getSignatureStatuses(request)
//
//            if (response.error != null) {
//                return TransactionStatus.PENDING
//            }
//
//            val statusInfo = response.result?.value?.firstOrNull()
//
//            when {
//                statusInfo == null -> TransactionStatus.PENDING
//                statusInfo.err != null -> TransactionStatus.FAILED
//                (statusInfo.confirmations ?: 0) >= 32 -> TransactionStatus.CONFIRMED
//                else -> TransactionStatus.PENDING
//            }
//        } catch (e: Exception) {
//            TransactionStatus.PENDING
//        }
//    }
//}
//
///**
// * Add these methods to TransactionDao:
// */
///*
//@Dao
//interface TransactionDao {
//    @Query("SELECT * FROM transactions WHERE status = 'PENDING'")
//    fun observePendingTransactions(): Flow<List<TransactionEntity>>
//
//    @Query("""
//        UPDATE transactions
//        SET status = :status,
//            confirmationCount = :confirmationCount,
//            blockNumber = :blockNumber,
//            errorMessage = :errorMessage
//        WHERE txHash = :txHash
//    """)
//    suspend fun updateTransactionStatus(
//        txHash: String,
//        status: String,
//        confirmationCount: Int,
//        blockNumber: Long?,
//        errorMessage: String? = null
//    )
//
//    @Query("UPDATE transactions SET confirmationCount = :confirmationCount WHERE txHash = :txHash")
//    suspend fun updateConfirmationCount(txHash: String, confirmationCount: Int)
//}
//*/