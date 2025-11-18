package com.octane.core.blockchain

import com.octane.core.network.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Manages Solana RPC endpoints with automatic fallback.
 * Monitors RPC health and switches to backup on failures.
 */
class SolanaRpcManager(
    private val networkMonitor: NetworkMonitor
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // RPC endpoints (in order of preference)
    private val endpoints = listOf(
        RpcEndpoint(
            url = "https://solana-mainnet.g.alchemy.com/v2/YOUR_KEY",
            type = RpcType.PRIMARY,
            name = "Alchemy"
        ),
        RpcEndpoint(
            url = "https://rpc.helius.xyz/?api-key=YOUR_KEY",
            type = RpcType.PRIMARY,
            name = "Helius"
        ),
        RpcEndpoint(
            url = "https://api.mainnet-beta.solana.com",
            type = RpcType.FALLBACK,
            name = "Public RPC"
        )
    )

    private val _currentEndpoint = MutableStateFlow(endpoints.first())
    val currentEndpoint: StateFlow<RpcEndpoint> = _currentEndpoint.asStateFlow()

    private val _endpointHealth = MutableStateFlow<Map<String, RpcHealth>>(emptyMap())
    val endpointHealth: StateFlow<Map<String, RpcHealth>> = _endpointHealth.asStateFlow()

    init {
        startHealthChecks()
    }

    /**
     * Get current RPC URL.
     */
    fun getCurrentRpcUrl(): String = _currentEndpoint.value.url

    /**
     * Switch to next available endpoint.
     */
    fun switchToNextEndpoint() {
        val currentIndex = endpoints.indexOf(_currentEndpoint.value)
        val nextIndex = (currentIndex + 1) % endpoints.size
        _currentEndpoint.value = endpoints[nextIndex]
    }

    /**
     * Set custom RPC endpoint.
     */
    fun setCustomEndpoint(url: String, name: String = "Custom") {
        val customEndpoint = RpcEndpoint(
            url = url,
            type = RpcType.CUSTOM,
            name = name
        )
        _currentEndpoint.value = customEndpoint
    }

    /**
     * Check health of all endpoints periodically.
     */
    private fun startHealthChecks() {
        scope.launch {
            while (isActive) {
                if (networkMonitor.isConnected.value) {
                    checkAllEndpoints()
                }
                delay(10.seconds)
            }
        }
    }

    private suspend fun checkAllEndpoints() {
        val healthMap = mutableMapOf<String, RpcHealth>()

        endpoints.forEach { endpoint ->
            val health = checkEndpointHealth(endpoint)
            healthMap[endpoint.url] = health

            // Auto-switch if current endpoint is down
            if (endpoint == _currentEndpoint.value && health.status == HealthStatus.DOWN) {
                switchToNextEndpoint()
            }
        }

        _endpointHealth.value = healthMap
    }

    private suspend fun checkEndpointHealth(endpoint: RpcEndpoint): RpcHealth {
        val startTime = System.currentTimeMillis()

        return try {
            // TODO: Implement actual RPC health check
            // Use getHealth or getVersion RPC methods

            val latency = System.currentTimeMillis() - startTime

            RpcHealth(
                endpoint = endpoint,
                latency = latency,
                status = when {
                    latency < 500 -> HealthStatus.HEALTHY
                    latency < 2000 -> HealthStatus.SLOW
                    else -> HealthStatus.DEGRADED
                },
                lastChecked = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            RpcHealth(
                endpoint = endpoint,
                latency = 0,
                status = HealthStatus.DOWN,
                lastChecked = System.currentTimeMillis(),
                error = e.message
            )
        }
    }
}

/**
 * RPC endpoint configuration.
 */
data class RpcEndpoint(
    val url: String,
    val type: RpcType,
    val name: String
)

enum class RpcType {
    PRIMARY,    // Paid, high-quality RPC (Helius, Alchemy)
    FALLBACK,   // Free public RPC (slower, rate-limited)
    CUSTOM      // User-provided RPC
}

/**
 * RPC endpoint health status.
 */
data class RpcHealth(
    val endpoint: RpcEndpoint,
    val latency: Long,
    val status: HealthStatus,
    val lastChecked: Long,
    val error: String? = null
)

enum class HealthStatus {
    HEALTHY,    // <500ms latency
    SLOW,       // 500ms-2s latency
    DEGRADED,   // >2s latency
    DOWN        // Unreachable or erroring
}
