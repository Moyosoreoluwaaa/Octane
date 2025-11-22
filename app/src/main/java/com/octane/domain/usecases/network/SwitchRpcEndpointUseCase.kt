package com.octane.domain.usecases.network

import com.octane.core.blockchain.SolanaRpcManager

/**
 * Switches to a different RPC endpoint.
 * Useful when primary endpoint is down or slow.
 */
class SwitchRpcEndpointUseCase(
    private val solanaRpcManager: SolanaRpcManager
) {
    operator fun invoke(customUrl: String? = null) {
        if (customUrl != null) {
            solanaRpcManager.setCustomEndpoint(customUrl)
        } else {
            solanaRpcManager.switchToNextEndpoint()
        }
    }
}