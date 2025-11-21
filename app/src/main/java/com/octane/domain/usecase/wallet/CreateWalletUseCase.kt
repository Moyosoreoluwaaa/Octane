package com.octane.domain.usecase.wallet

import com.octane.core.blockchain.SolanaKeyGenerator
import com.octane.core.security.KeystoreManager
import com.octane.domain.models.Wallet
import com.octane.domain.repository.WalletRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

/**
 * Creates a new wallet with generated keys.
 *
 * Business Rules:
 * - Wallet name must not be empty
 * - First wallet is automatically set as active
 * - Generates unique emoji and color for visual identification
 * - Stores encrypted private key in secure storage
 *
 * Usage:
 * ```
 * val result = createWalletUseCase(name = "My Wallet", emoji = "🔥")
 * ```
 */

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
