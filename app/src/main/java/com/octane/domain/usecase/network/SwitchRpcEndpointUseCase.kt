package com.octane.domain.usecase.network

import com.octane.core.blockchain.SolanaRpcManager
import javax.inject.Inject

/**
 * Switches to a different RPC endpoint.
 * Useful when primary endpoint is down or slow.
 */
class SwitchRpcEndpointUseCase @Inject constructor(
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