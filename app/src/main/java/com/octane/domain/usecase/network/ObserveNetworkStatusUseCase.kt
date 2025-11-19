package com.octane.domain.usecase.network

import com.octane.core.network.NetworkMonitor
import com.octane.core.blockchain.SolanaRpcManager
import com.octane.core.network.ConnectionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Observes network connectivity status.
 * Used to show offline banners and disable network-dependent features.
 */
class ObserveNetworkStatusUseCase @Inject constructor(
    private val networkMonitor: NetworkMonitor
) {
    operator fun invoke(): Flow<NetworkStatus> {
        return combine(
            networkMonitor.isConnected,
            networkMonitor.connectionType
        ) { connected, type ->
            NetworkStatus(
                isConnected = connected,
                connectionType = type,
                isMetered = type == ConnectionType.CELLULAR
            )
        }
    }
}

data class NetworkStatus(
    val isConnected: Boolean,
    val connectionType: ConnectionType,
    val isMetered: Boolean
)
