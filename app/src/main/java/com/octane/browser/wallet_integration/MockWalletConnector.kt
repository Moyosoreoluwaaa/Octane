package com.octane.browser.wallet_integration

import com.octane.browser.wallet_integration.models.TransactionRequest

/**
 * Mock implementation for testing
 * Replace with real WalletConnector when wallet module is ready
 */
class MockWalletConnector : WalletConnector {
    
    private val connectedDomains = mutableMapOf<String, String>()
    
    override suspend fun requestConnection(domain: String, chainId: String): Result<String> {
        // Mock address
        val address = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"
        connectedDomains[domain] = address
        return Result.success(address)
    }
    
    override suspend fun getConnectedAddress(domain: String): String? {
        return connectedDomains[domain]
    }
    
    override suspend fun signMessage(message: String, address: String): Result<String> {
        // Mock signature
        return Result.success("0xabcd1234...signature")
    }
    
    override suspend fun sendTransaction(tx: TransactionRequest): Result<String> {
        // Mock transaction hash
        return Result.success("0x123abc...txhash")
    }
    
    override fun isConnected(domain: String): Boolean {
        return connectedDomains.containsKey(domain)
    }
    
    override fun disconnect(domain: String) {
        connectedDomains.remove(domain)
    }
}