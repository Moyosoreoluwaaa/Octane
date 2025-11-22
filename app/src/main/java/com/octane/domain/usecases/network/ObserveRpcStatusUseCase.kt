// domain/usecases/network/ObserveRpcStatusUseCase.kt

package com.octane.domain.usecases.network

import com.octane.core.blockchain.SolanaRpcManager
import com.octane.domain.models.NetworkHealth
import kotlinx.coroutines.flow.Flow

/**
 * Observes the status of the current RPC endpoint.
 * Returns health status for the Network Status Indicator (V1.9).
 */
class ObserveRpcStatusUseCase(
    private val rpcManager: SolanaRpcManager
) {
    operator fun invoke(): Flow<NetworkHealth> {
        return rpcManager.observeNetworkHealth()
    }
}