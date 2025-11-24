package com.octane.data.mappers

import com.octane.data.local.database.entities.DAppEntity
import com.octane.data.remote.dto.DAppDto
import com.octane.domain.models.DApp
import com.octane.domain.models.DAppCategory


/**
 * DTO → Entity
 */
fun DAppDto.toEntity(): DAppEntity {
    return DAppEntity(
        id = id,
        name = name,
        description = description,
        logoUrl = logo,
        category = category,
        url = url,
        tvl = tvl,
        volume24h = volume24h,
        users24h = users24h,
        isVerified = verified ?: true,
        chains = chains?.joinToString(",") ?: "Solana",
        rating = rating ?: 0.0,
        tags = tags?.joinToString(",") ?: "",
        lastUpdated = System.currentTimeMillis()
    )
}

/**
 * Entity → Domain
 */
fun DAppEntity.toDomain(): DApp {
    return DApp(
        id = id,
        name = name,
        description = description,
        logoUrl = logoUrl,
        category = parseDAppCategory(category),
        url = url,
        tvl = tvl,
        volume24h = volume24h,
        users24h = users24h,
        isVerified = isVerified,
        chains = chains.split(",").filter { it.isNotBlank() },
        rating = rating,
        tags = tags.split(",").filter { it.isNotBlank() }
    )
}

/**
 * Parse category string to enum.
 */
private fun parseDAppCategory(category: String): DAppCategory {
    return when (category.lowercase()) {
        "defi", "dex", "lending" -> DAppCategory.DEFI
        "nft", "marketplace" -> DAppCategory.NFT
        "gaming", "game" -> DAppCategory.GAMING
        "social" -> DAppCategory.SOCIAL
        "bridge" -> DAppCategory.BRIDGE
        "wallet" -> DAppCategory.WALLET
        "tools", "developer" -> DAppCategory.TOOLS
        else -> DAppCategory.OTHER
    }
}

fun List<DAppDto>.toEntities(): List<DAppEntity> = map { it.toEntity() }
fun List<DAppEntity>.toDomainDApps(): List<DApp> = map { it.toDomain() }