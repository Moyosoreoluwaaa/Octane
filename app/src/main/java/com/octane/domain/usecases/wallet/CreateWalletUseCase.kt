package com.octane.domain.usecases.wallet

import cash.z.ecc.android.bip39.Mnemonics
import com.octane.core.blockchain.SolanaKeyGenerator
import com.octane.core.security.KeystoreManager
import com.octane.domain.models.Wallet
import com.octane.domain.repository.WalletRepository
import kotlinx.coroutines.flow.first
import java.security.SecureRandom
import java.util.UUID

/**
 * Creates a new wallet with generated keys.
 * NOW RETURNS SEED PHRASE for backup screen.
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
    ): Result<WalletCreationResult> {
        return try {
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Wallet name cannot be empty"))
            }

            // Generate 12-word BIP39 mnemonic
            val entropy = ByteArray(16) // 128 bits = 12 words
            SecureRandom().nextBytes(entropy)
            val mnemonicCode = Mnemonics.MnemonicCode(entropy)
            val seedPhrase = mnemonicCode.words.joinToString(" ")

            // Generate keypair from seed phrase
            val keypair = solanaKeyGenerator.fromSeedPhrase(seedPhrase)

            // Check if first wallet
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
                isActive = isFirstWallet,
                isHardwareWallet = false,
                hardwareDeviceName = null,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )

            // Store encrypted private key
            keystoreManager.storePrivateKey(wallet.id, keypair.privateKey).getOrThrow()

            // Save wallet to database
            walletRepository.createWallet(wallet)

            // Return wallet + seed phrase
            Result.success(
                WalletCreationResult(
                    wallet = wallet,
                    seedPhrase = seedPhrase
                )
            )
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

data class WalletCreationResult(
    val wallet: Wallet,
    val seedPhrase: String
)