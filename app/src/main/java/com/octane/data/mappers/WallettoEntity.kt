package com.octane.data.mappers

import com.octane.data.local.database.entities.WalletEntity
import com.octane.domain.models.Wallet


/**
 * Domain → Entity
 */
fun Wallet.toEntity(): WalletEntity = WalletEntity(
    id = id,
    name = name,
    publicKey = publicKey,
    iconEmoji = iconEmoji,
    colorHex = colorHex,
    chainId = chainId,
    isActive = isActive,
    isHardwareWallet = isHardwareWallet,
    hardwareDeviceName = hardwareDeviceName,
    createdAt = createdAt,
    lastUpdated = lastUpdated
)
