package com.octane.data.mappers

import com.octane.data.local.database.entities.AssetEntity
import com.octane.domain.models.Asset

fun AssetEntity.toDomain(): Asset = Asset(
    id = id,
    walletId = walletId,
    chainId = chainId,
    symbol = symbol,
    name = name,
    mintAddress = mintAddress,
    balance = balance,
    decimals = decimals,
    priceUsd = priceUsd,
    valueUsd = valueUsd,
    priceChange24h = priceChange24h,
    iconUrl = iconUrl,
    isNative = isNative,
    isHidden = isHidden,
    costBasisUsd = costBasisUsd,
    lastUpdated = lastUpdated
)

fun Asset.toEntity(): AssetEntity = AssetEntity(
    id = id,
    walletId = walletId,
    chainId = chainId,
    symbol = symbol,
    name = name,
    mintAddress = mintAddress,
    balance = balance,
    decimals = decimals,
    priceUsd = priceUsd,
    valueUsd = valueUsd,
    priceChange24h = priceChange24h,
    iconUrl = iconUrl,
    isNative = isNative,
    isHidden = isHidden,
    costBasisUsd = costBasisUsd,
    lastUpdated = lastUpdated
)
