// Estimate Transaction Fee
class EstimateTransactionFeeUseCase(
private val solanaRpcApi: SolanaRpcApi,
private val walletRepository: WalletRepository
) {
suspend operator fun invoke(
tokenSymbol: String,
amount: Double
): Result<Double> {
return try {
// Get recent blockhash for fee calculation
val response = solanaRpcApi.getRecentBlockhash(
RpcRequest(method = "getRecentBlockhash", params = emptyList())
)

            // Solana base fee is ~0.000005 SOL per signature
            // Add priority fee if network is congested
            val baseFee = 0.000005
            val priorityFee = calculatePriorityFee(response)

            Result.success(baseFee + priorityFee)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculatePriorityFee(response: Any): Double {
        // TODO: Implement priority fee calculation based on network load
        return 0.0
    }
}

class SendTokenUseCase(
private val solanaRpcApi: SolanaRpcApi,
private val transactionRepository: TransactionRepository,
private val walletRepository: WalletRepository,
private val keystoreManager: KeystoreManager
) {
suspend operator fun invoke(
recipient: String,
tokenSymbol: String,
amount: Double
): Result<String> {
return try {
// Get active wallet
val wallet = walletRepository.observeActiveWallet().first()
?: return Result.failure(IllegalStateException("No active wallet"))

            // Get private key
            val privateKey = keystoreManager.getPrivateKey(wallet.id)
                .getOrThrow()

            // Get token mint address
            val tokenMint = getTokenMintAddress(tokenSymbol)

            // Build transaction
            val transaction = buildSendTransaction(
                sender = wallet.publicKey,
                recipient = recipient,
                amount = amount,
                tokenMint = tokenMint
            )

            // Sign transaction
            val signedTx = signTransaction(transaction, privateKey)

            // Submit to RPC
            val response = solanaRpcApi.sendTransaction(
                RpcRequest(
                    method = "sendTransaction",
                    params = listOf(signedTx)
                )
            )

            val txHash = response.result as String

            // Store in database with CORRECT Transaction model fields
            transactionRepository.insertTransaction(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    walletId = wallet.id,
                    chainId = "solana", // ADDED
                    txHash = txHash,
                    type = TransactionType.SEND,
                    status = TransactionStatus.PENDING,
                    fromAddress = wallet.publicKey, // ADDED
                    toAddress = recipient, // CHANGED from recipientAddress
                    amount = amount.toString(), // CHANGED to String
                    tokenSymbol = tokenSymbol,
                    tokenMint = tokenMint, // ADDED
                    fee = "0.000005", // CHANGED to String
                    feePriority = null, // ADDED
                    blockNumber = null, // ADDED
                    confirmationCount = 0, // ADDED
                    errorMessage = null, // ADDED
                    memo = null, // ADDED
                    timestamp = System.currentTimeMillis(),
                    simulated = false, // ADDED
                    simulationSuccess = null // ADDED
                )
            )

            Result.success(txHash)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getTokenMintAddress(symbol: String): String {
        return when (symbol) {
            "SOL" -> "So11111111111111111111111111111111111111112"
            "USDC" -> "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"
            "USDT" -> "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB"
            "BONK" -> "DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263"
            else -> throw IllegalArgumentException("Unsupported token: $symbol")
        }
    }

    private fun buildSendTransaction(
        sender: String,
        recipient: String,
        amount: Double,
        tokenMint: String
    ): String {
        // TODO: Implement Solana transaction building
        // Libraries:
        // - sol4k (pure Kotlin): https://github.com/sol4k/sol4k
        // - Solana Kotlin SDK: https://github.com/metaplex-foundation/solana-kotlin
        return ""
    }

    private fun signTransaction(transaction: String, privateKey: EncryptedPrivateKey): String {
        // TODO: Implement transaction signing with Ed25519
        return ""
    }
}

class SetActiveWalletUseCase(
private val walletRepository: WalletRepository
) {
suspend operator fun invoke(walletId: String): Result<Unit> {
return try {
walletRepository.setActiveWallet(walletId)
Result.success(Unit)
} catch (e: Exception) {
Result.failure(e)
}
}
}

class SwapTokensUseCase(
private val jupiterApi: JupiterApiService,
private val transactionRepository: TransactionRepository,
private val walletRepository: WalletRepository,
private val keystoreManager: KeystoreManager,
private val solanaRpcApi: SolanaRpcApi
) {
suspend operator fun invoke(
fromToken: String,
toToken: String,
amount: Double,
slippageBps: Int = 100 // 1% default
): Result<String> {
return try {
// Get active wallet
val wallet = walletRepository.observeActiveWallet().first()
?: return Result.failure(IllegalStateException("No active wallet"))

            // Get token mint addresses
            val inputMint = getTokenMintAddress(fromToken)
            val outputMint = getTokenMintAddress(toToken)

            // Convert amount to lamports (smallest unit)
            val amountLamports = (amount * 1_000_000_000).toLong()

            // Get quote from Jupiter
            val quote = jupiterApi.getQuote(
                inputMint = inputMint,
                outputMint = outputMint,
                amount = amountLamports,
                slippageBps = slippageBps
            )

            // Get swap transaction
            val swapRequest = JupiterSwapRequest(
                quoteResponse = quote,
                userPublicKey = wallet.publicKey,
                wrapAndUnwrapSol = true,
                useSharedAccounts = true
            )

            val swapResponse = jupiterApi.getSwapTransaction(swapRequest)

            // Sign transaction
            val privateKey = keystoreManager.getPrivateKey(wallet.id).getOrThrow()
            val signedTx = signTransaction(swapResponse.swapTransaction, privateKey)

            // Submit transaction
            val rpcResponse = solanaRpcApi.sendTransaction(
                RpcRequest(
                    method = "sendTransaction",
                    params = listOf(signedTx)
                )
            )

            val txHash = rpcResponse.result as String

            // Calculate output amount
            val outAmountDecimal = quote.outAmount.toLong() / 1_000_000_000.0

            // Store in database with CORRECT fields
            transactionRepository.insertTransaction(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    walletId = wallet.id,
                    chainId = "solana",
                    txHash = txHash,
                    type = TransactionType.SWAP,
                    status = TransactionStatus.PENDING,
                    fromAddress = wallet.publicKey,
                    toAddress = wallet.publicKey, // Swap to self
                    amount = amount.toString(),
                    tokenSymbol = fromToken,
                    tokenMint = inputMint,
                    fee = (swapResponse.prioritizationFeeLamports / 1_000_000_000.0).toString(),
                    feePriority = "medium", // Based on Jupiter's recommendation
                    blockNumber = null,
                    confirmationCount = 0,
                    errorMessage = null,
                    memo = "Swapped $amount $fromToken → ${outAmountDecimal} $toToken",
                    timestamp = System.currentTimeMillis(),
                    simulated = false,
                    simulationSuccess = null
                )
            )

            Result.success(txHash)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getTokenMintAddress(symbol: String): String {
        return when (symbol) {
            "SOL" -> "So11111111111111111111111111111111111111112"
            "USDC" -> "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"
            "USDT" -> "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB"
            "BONK" -> "DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263"
            "JUP" -> "JUPyiwrYJFskUPiHa7hkeR8VUtAeFoSYbKedZNsDvCN"
            "RAY" -> "4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R"
            "ORCA" -> "orcaEKTdK7LKz57vaAYr9QeNsVEPfiu6QeMU1kektZE"
            else -> throw IllegalArgumentException("Unsupported token: $symbol")
        }
    }

    private fun signTransaction(transaction: String, privateKey: EncryptedPrivateKey): String {
        // TODO: Implement transaction signing
        // Jupiter returns base64-encoded transaction
        // 1. Decode from base64
        // 2. Sign with Ed25519
        // 3. Re-encode to base64
        return ""
    }
}

class ValidateSolanaAddressUseCase {
operator fun invoke(address: String): Boolean {
// Solana addresses are base58-encoded, 32-44 characters
if (address.length !in 32..44) return false

        // Check for valid base58 characters
        val base58Regex = "^[1-9A-HJ-NP-Za-km-z]+$".toRegex()
        if (!address.matches(base58Regex)) return false

        // TODO: Add SNS domain resolution (@alice.sol)
        if (address.endsWith(".sol")) {
            // Resolve SNS domain to public key
            return true // For now
        }

        return true
    }
}

class CreateWalletUseCase(
private val walletRepository: WalletRepository,
private val keystoreManager: KeystoreManager,
private val solanaKeyGenerator: SolanaKeyGenerator
) {
suspend operator fun invoke(
name: String,
iconEmoji: String? = null,
colorHex: String? = null
): Result<Wallet> {
return try {
// Validate input
if (name.isBlank()) {
return Result.failure(IllegalArgumentException("Wallet name cannot be empty"))
}

            // Generate new Solana keypair
            val keypair = solanaKeyGenerator.generateKeypair()

            // Check if this is the first wallet
            val existingCount = walletRepository.observeWalletCount().first()
            val isFirstWallet = existingCount == 0

            // Create wallet model
            val wallet = Wallet(
                id = UUID.randomUUID().toString(),
                name = name,
                publicKey = keypair.publicKey,
                iconEmoji = iconEmoji ?: generateRandomEmoji(),
                colorHex = colorHex ?: generateRandomColor(),
                chainId = "solana",
                isActive = isFirstWallet, // First wallet is active by default
                isHardwareWallet = false,
                hardwareDeviceName = null,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )

            // Store encrypted private key
            keystoreManager.storePrivateKey(wallet.id, keypair.privateKey)
                .getOrThrow()

            // Save wallet to database
            walletRepository.createWallet(wallet)

            Result.success(wallet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateRandomEmoji(): String {
        val emojis = listOf("🔥", "⚡", "💎", "🚀", "🌟", "🎯", "💰", "🏆")
        return emojis.random()
    }

    private fun generateRandomColor(): String {
        val colors = listOf(
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A",
            "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2"
        )
        return colors.random()
    }
}

class DeleteWalletUseCase(
private val walletRepository: WalletRepository,
private val keystoreManager: KeystoreManager
) {
suspend operator fun invoke(walletId: String): Result<Unit> {
return try {
// Check wallet count
val count = walletRepository.observeWalletCount().first()
if (count <= 1) {
return Result.failure(
IllegalStateException("Cannot delete the only wallet")
)
}

            // Check if deleting active wallet
            val wallet = walletRepository.getWalletById(walletId)
            val needsNewActive = wallet?.isActive == true

            // Delete private key from secure storage
            keystoreManager.deletePrivateKey(walletId)

            // Delete wallet from database
            walletRepository.deleteWallet(walletId)

            // Activate another wallet if needed
            if (needsNewActive) {
                val remainingWallets = walletRepository.observeAllWallets().first()
                remainingWallets.firstOrNull()?.let { nextWallet ->
                    walletRepository.setActiveWallet(nextWallet.id)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class ImportWalletUseCase(
private val walletRepository: WalletRepository,
private val keystoreManager: KeystoreManager,
private val solanaKeyGenerator: SolanaKeyGenerator
) {
suspend operator fun invoke(
seedPhrase: String,
name: String,
iconEmoji: String? = null,
colorHex: String? = null
): Result<Wallet> {
return try {
// Validate seed phrase
val words = seedPhrase.trim().split("\\s+".toRegex())
if (words.size != 12 && words.size != 24) {
return Result.failure(
IllegalArgumentException("Seed phrase must be 12 or 24 words")
)
}

            // Derive keypair from seed phrase
            val keypair = solanaKeyGenerator.fromSeedPhrase(seedPhrase)

            // Check if wallet already exists
            val existingWallet = walletRepository.getWalletByPublicKey(keypair.publicKey)
            if (existingWallet != null) {
                return Result.failure(
                    IllegalStateException("Wallet already imported")
                )
            }

            // Create wallet model (not active by default)
            val wallet = Wallet(
                id = UUID.randomUUID().toString(),
                name = name.ifBlank { "Imported Wallet" },
                publicKey = keypair.publicKey,
                iconEmoji = iconEmoji ?: "📥",
                colorHex = colorHex ?: "#4ECDC4",
                chainId = "solana",
                isActive = false,
                isHardwareWallet = false,
                hardwareDeviceName = null,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )

            // Store encrypted private key
            keystoreManager.storePrivateKey(wallet.id, keypair.privateKey)
                .getOrThrow()

            // Save wallet
            walletRepository.createWallet(wallet)

            Result.success(wallet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class ObserveWalletsUseCase(
private val walletRepository: WalletRepository
) {
operator fun invoke(): Flow<LoadingState<List<Wallet>>> {
return walletRepository.observeAllWallets()
.map<List<Wallet>, LoadingState<List<Wallet>>> { wallets ->
LoadingState.Success(wallets)
}
.catch { e ->
emit(LoadingState.Error(e) as LoadingState<List<Wallet>>)
}
}
}
class SwitchActiveWalletUseCase (
private val walletRepository: WalletRepository,
private val userPreferencesStore: UserPreferencesStore
) {
suspend operator fun invoke(walletId: String): Result<Unit> {
return try {
// Validate wallet exists
val wallet = walletRepository.getWalletById(walletId)
?: return Result.failure(IllegalArgumentException("Wallet not found"))

            // Set as active
            walletRepository.setActiveWallet(walletId)
            
            // Save to preferences for persistence
            userPreferencesStore.setLastActiveWalletId(walletId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
class MonitorPendingTransactionsUseCase(
private val transactionRepository: TransactionRepository,
private val solanaRpcApi: SolanaRpcApi
) {
suspend operator fun invoke() {
val pendingTxs = transactionRepository
.observePendingTransactions().first()

        pendingTxs.forEach { tx ->
            try {
                // Query transaction status
                val response = solanaRpcApi.getSignatureStatuses(
                    RpcRequest(
                        method = "getSignatureStatuses",
                        params = listOf(listOf(tx.txHash))
                    )
                )

                val status = response.result?.value?.firstOrNull()

                if (status != null) {
                    val newStatus = when {
                        status.err != null -> TransactionStatus.FAILED
                        status.confirmationStatus == "finalized" -> TransactionStatus.CONFIRMED
                        else -> TransactionStatus.PENDING
                    }

                    transactionRepository.updateTransactionStatus(
                        txId = tx.id,
                        status = newStatus,
                        confirmationCount = status.confirmations ?: 0,
                        errorMessage = status.err?.toString()
                    )
                }
            } catch (e: Exception) {
                // Log error but continue processing other transactions
                println("Error monitoring transaction ${tx.txHash}: ${e.message}")
            }
        }
    }
}

class ObserveTransactionHistoryUseCase(
private val transactionRepository: TransactionRepository,
private val walletRepository: WalletRepository
) {
@OptIn(ExperimentalCoroutinesApi::class)
operator fun invoke(limit: Int = 50): Flow<LoadingState<List<Transaction>>> {
return walletRepository.observeActiveWallet()
.flatMapLatest { wallet ->
if (wallet == null) {
flowOf(LoadingState.Error(IllegalStateException("No active wallet")))
} else {
transactionRepository.observeRecentTransactions(wallet.id, limit)
.map { txs -> LoadingState.Success(txs) }
}
}
}
}

class SendSolUseCase(
private val transactionRepository: TransactionRepository,
private val walletRepository: WalletRepository,
private val assetRepository: AssetRepository,
private val transactionSimulator: TransactionSimulator,
private val maliciousSignatureDetector: MaliciousSignatureDetector,
private val transactionBuilder: TransactionBuilder,
private val keystoreManager: KeystoreManager,
private val solanaKeyGenerator: SolanaKeyGenerator,
private val solanaRpcApi: SolanaRpcApi
) {
/**
* Step 1: Simulate transaction to preview effects.
*/
suspend fun simulate(
recipientAddress: String,
amountSol: Double,
memo: String? = null
): SimulationResult {
// Validate inputs
if (!Validators.isValidSolanaAddress(recipientAddress)) {
return SimulationResult.Error(
message = "Invalid Solana address",
logs = emptyList()
)
}

        if (amountSol < 0.000001) {
            return SimulationResult.Error(
                message = "Amount too small (minimum 0.000001 SOL)",
                logs = emptyList()
            )
        }

        // Get active wallet
        val wallet = walletRepository.observeActiveWallet().first()
            ?: return SimulationResult.Error(
                message = "No active wallet",
                logs = emptyList()
            )

        // Check balance
        val solAsset = assetRepository.observeAsset(wallet.id, "SOL").first()
        val currentBalance = solAsset?.balanceDouble ?: 0.0

        if (currentBalance < amountSol + 0.001) { // Reserve for rent
            return SimulationResult.Error(
                message = "Insufficient balance (need ${amountSol + 0.001} SOL)",
                logs = emptyList()
            )
        }

        // Build transaction
        val lamports = (amountSol * 1_000_000_000).toLong()
        val txBytes = transactionBuilder.buildTransfer(
            from = wallet.publicKey,
            to = recipientAddress,
            lamports = lamports,
            memo = memo
        )

        // Simulate
        return transactionSimulator.simulate(txBytes)
    }

    /**
     * Step 2: Check for malicious signatures.
     */
    suspend fun scanForMaliciousActivity(
        recipientAddress: String,
        amountSol: Double
    ): TransactionRiskAssessment {
        val wallet = walletRepository.observeActiveWallet().first()!!

        val lamports = (amountSol * 1_000_000_000).toLong()
        val txBytes = transactionBuilder.buildTransfer(
            from = wallet.publicKey,
            to = recipientAddress,
            lamports = lamports
        )

        return maliciousSignatureDetector.analyzeTransaction(txBytes)
    }

    /**
     * Step 3: Execute transaction after user confirmation.
     */
    suspend operator fun invoke(
        recipientAddress: String,
        amountSol: Double,
        memo: String? = null,
        priorityFeeMicroLamports: Long = 10_000
    ): Result<Transaction> {
        return try {
            val wallet = walletRepository.observeActiveWallet().first()!!

            // Build and sign transaction
            val lamports = (amountSol * 1_000_000_000).toLong()
            val txBytes = transactionBuilder.buildTransfer(
                from = wallet.publicKey,
                to = recipientAddress,
                lamports = lamports,
                memo = memo
            )

            // Retrieve and decrypt private key
            val encryptedKey = keystoreManager.getPrivateKey(wallet.id).getOrThrow()
            val privateKey = keystoreManager.decryptPrivateKey(encryptedKey).getOrThrow()

            // Sign transaction
            val signedTx = solanaKeyGenerator.signTransaction(txBytes, privateKey)

            // Clear private key from memory
            privateKey.fill(0)

            // Broadcast to network
            val response = solanaRpcApi.sendTransaction(
                RpcRequest(
                    method = "sendTransaction",
                    params = listOf(signedTx.toBase64())
                )
            )

            val txHash = response.result ?: throw Exception(
                response.error?.message ?: "Failed to broadcast transaction"
            )

            // Create transaction record
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                walletId = wallet.id,
                chainId = "solana",
                txHash = txHash,
                type = TransactionType.SEND,
                status = TransactionStatus.PENDING,
                fromAddress = wallet.publicKey,
                toAddress = recipientAddress,
                amount = amountSol.toString(),
                tokenSymbol = "SOL",
                tokenMint = null,
                fee = (priorityFeeMicroLamports / 1_000_000_000.0).toString(),
                feePriority = "medium",
                blockNumber = null,
                confirmationCount = 0,
                errorMessage = null,
                memo = memo,
                timestamp = System.currentTimeMillis(),
                simulated = false,
                simulationSuccess = null
            )

            // Save to database
            transactionRepository.insertTransaction(transaction)

            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


class AuthenticateWithBiometricsUseCase(
private val biometricManager: BiometricManager
) {
/**
* Authenticate user with biometrics.
*
* @param activity Current FragmentActivity (required for biometric prompt)
* @param title Prompt title
* @param subtitle Optional subtitle
* @param description Optional description
* @return Result<Unit> - Success if authenticated, Failure if cancelled/error
*/
suspend operator fun invoke(
activity: FragmentActivity,
title: String = "Authenticate",
subtitle: String? = null,
description: String? = null
): Result<Unit> = suspendCancellableCoroutine { continuation ->
biometricManager.authenticate(
activity = activity,
config = BiometricConfig(
title = title,
subtitle = subtitle,
description = description
),
onSuccess = {
// onCancellation callback - cleanup if coroutine cancelled
continuation.resume(Result.success(Unit)) { cause, _, _ -> // onCancellation callback - cleanup if coroutine cancelled
// onCancellation callback - cleanup if coroutine cancelled
}
},
onError = { errorCode, message ->
// onCancellation callback
continuation.resume(
Result.failure(SecurityException("Biometric auth failed: $message"))
) { cause, _, _ -> // onCancellation callback
// onCancellation callback
}
}
)
}
}

class CheckBiometricAvailabilityUseCase(
private val biometricManager: BiometricManager
) {
operator fun invoke(): BiometricAvailability {
return biometricManager.isBiometricAvailable()
}
}


class ObserveCurrencyPreferenceUseCase(
private val preferencesStore: UserPreferencesStore
) {
operator fun invoke(): Flow<String> {
return preferencesStore.currencyPreference
}
}

class TogglePrivacyModeUseCase(
private val preferencesStore: UserPreferencesStore
) {
suspend operator fun invoke(enabled: Boolean) {
preferencesStore.setPrivacyMode(enabled)
}
}

class UpdateCurrencyPreferenceUseCase (
private val preferencesStore: UserPreferencesStore
) {
suspend operator fun invoke(currency: String) {
preferencesStore.setCurrency(currency)
}
}

class ObserveNetworkStatusUseCase(
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

class ObservePortfolioUseCase(
private val assetRepository: AssetRepository,
private val walletRepository: WalletRepository
) {
operator fun invoke(): Flow<LoadingState<PortfolioState>> {
return walletRepository.observeActiveWallet()
.combine(assetRepository.observeAssets()) { wallet, assets ->
if (wallet == null) {
return@combine LoadingState.Error(
IllegalStateException("No active wallet")
)
}

                val visibleAssets = assets.filter { !it.isHidden }
                val totalValue = visibleAssets.sumOf { it.valueUsd ?: 0.0 }
                val total24hChange = calculatePortfolioChange(visibleAssets)

                LoadingState.Success(
                    PortfolioState(
                        assets = visibleAssets.sortedByDescending { it.valueUsd },
                        totalValueUsd = totalValue,
                        change24hPercent = total24hChange
                    )
                )
            }
    }

    private fun calculatePortfolioChange(assets: List<Asset>): Double {
        val totalValue = assets.sumOf { it.valueUsd ?: 0.0 }
        if (totalValue == 0.0) return 0.0

        val totalChange = assets.sumOf { asset ->
            val value = asset.valueUsd ?: 0.0
            val change = asset.priceChange24h ?: 0.0
            value * (change / 100.0)
        }

        return (totalChange / totalValue) * 100.0
    }
}

data class PortfolioState(
val assets: List<Asset>,
val totalValueUsd: Double,
val change24hPercent: Double
)

class RefreshAssetsUseCase(
private val assetRepository: AssetRepository,
private val walletRepository: WalletRepository,
private val networkMonitor: NetworkMonitor
) {
suspend operator fun invoke(): LoadingState<Unit> {
// Check network connectivity
if (!networkMonitor.isConnected.value) {
return LoadingState.Error(
Exception("No internet connection. Showing cached data.")
)
}

        // Get active wallet
        val wallet = walletRepository.observeActiveWallet().first()
            ?: return LoadingState.Error(
                IllegalStateException("No active wallet")
            )

        // Refresh assets
        return assetRepository.refreshAssets(wallet.id, wallet.publicKey)
    }
}

class ToggleAssetVisibilityUseCase(
private val assetRepository: AssetRepository
) {
suspend operator fun invoke(
assetId: String,
isHidden: Boolean
): Result<Unit> {
return try {
assetRepository.updateAssetVisibility(assetId, isHidden)
Result.success(Unit)
} catch (e: Exception) {
Result.failure(e)
}
}
}


---

Models

data class Approval(
val id: String,
val walletId: String,
val chainId: String,
val tokenMint: String,
val tokenSymbol: String,
val spenderAddress: String,
val spenderName: String?,
val allowance: String,
val isRevoked: Boolean,
val approvedAt: Long,
val revokedAt: Long?
)


data class Asset(
val id: String,
val walletId: String,
val chainId: String,
val symbol: String,
val name: String,
val mintAddress: String?,
val balance: String,
val decimals: Int,
val priceUsd: Double? = null,
val valueUsd: Double? = null,
val priceChange24h: Double? = null,
val iconUrl: String? = null,
val isNative: Boolean = false,
val isHidden: Boolean = false,
val costBasisUsd: Double? = null,
val lastUpdated: Long
) {
val balanceDouble: Double
get() = balance.toDoubleOrNull() ?: 0.0

    val profitLossUsd: Double?
        get() = if (valueUsd != null && costBasisUsd != null) {
            valueUsd - costBasisUsd
        } else null
    
    val profitLossPercentage: Double?
        get() = if (profitLossUsd != null && costBasisUsd != null && costBasisUsd > 0) {
            (profitLossUsd!! / costBasisUsd) * 100
        } else null
}

data class Contact(
val id: String,
val name: String,
val address: String,
val chainId: String,
val createdAt: Long,
val lastUsed: Long
)

sealed interface NetworkHealth {
data object Unknown : NetworkHealth
data object Offline : NetworkHealth
data class Healthy(val latencyMs: Long) : NetworkHealth
data class Slow(val latencyMs: Long) : NetworkHealth
data class Degraded(val latencyMs: Long) : NetworkHealth
data class Down(val error: String) : NetworkHealth
}

data class StakingPosition(
val id: String,
val walletId: String,
val chainId: String,
val validatorAddress: String,
val validatorName: String,
val amountStaked: String,
val rewardsEarned: String,
val apy: Double,
val isActive: Boolean,
val stakedAt: Long,
val unstakedAt: Long?
)

enum class TransactionType {
SEND,
RECEIVE,
SWAP,
STAKE,
UNSTAKE,
CLAIM_REWARDS,
APPROVE,
REVOKE,
NFT_MINT,
NFT_TRANSFER
}

enum class TransactionStatus {
PENDING,
CONFIRMED,
FAILED
}

data class Transaction(
val id: String,
val walletId: String,
val chainId: String,
val txHash: String,
val type: TransactionType,
val status: TransactionStatus,
val fromAddress: String,
val toAddress: String?,
val amount: String,
val tokenSymbol: String,
val tokenMint: String?,
val fee: String,
val feePriority: String?,
val blockNumber: Long?,
val confirmationCount: Int,
val errorMessage: String?,
val memo: String?,
val timestamp: Long,
val simulated: Boolean = false,
val simulationSuccess: Boolean? = null
)

data class Wallet(
val id: String,
val name: String,
val publicKey: String,
val iconEmoji: String? = null,
val colorHex: String? = null,
val chainId: String = "solana",
val isActive: Boolean = false,
val isHardwareWallet: Boolean = false,
val hardwareDeviceName: String? = null,
val createdAt: Long,
val lastUpdated: Long
)
