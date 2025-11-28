package com.octane.browser.wallet_integration

import com.octane.browser.wallet_integration.models.TransactionRequest

/**
 * Interface to communicate with the wallet module
 * This is the bridge between browser and wallet
 */
interface WalletConnector {
    suspend fun requestConnection(domain: String, chainId: String): Result<String>
    suspend fun getConnectedAddress(domain: String): String?
    suspend fun signMessage(message: String, address: String): Result<String>
    suspend fun sendTransaction(tx: TransactionRequest): Result<String>
    fun isConnected(domain: String): Boolean
    fun disconnect(domain: String)
}