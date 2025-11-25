package com.octane.data.mappers

import com.octane.data.local.database.entities.DAppEntity
import com.octane.data.remote.dto.DAppDto
import com.octane.domain.models.DApp
import com.octane.domain.models.DAppCategory

/**
 * ✅ FIXED: DTO → Entity with proper null handling
 */
fun DAppDto.toEntity(): DAppEntity {
    return DAppEntity(
        id = id,
        name = name,
        description = description ?: "No description available",
        logoUrl = logo,
        category = category,
        url = url ?: "https://defillama.com/protocol/$slug",
        tvl = tvl,
        volume24h = null, // Not in /protocols endpoint
        users24h = null, // Not in /protocols endpoint
        isVerified = audits != null && audits != "0",
        chains = chains.joinToString(",").ifEmpty { chain ?: "Solana" },
        rating = 0.0, // Not in API
        tags = (oracles ?: emptyList()).joinToString(","),
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
    return when (category.lowercase().replace(" ", "")) {
        "dex", "decentralizedexchange" -> DAppCategory.DEFI
        "lending", "borrowing" -> DAppCategory.DEFI
        "yield", "yieldaggregator" -> DAppCategory.DEFI
        "liquidstaking" -> DAppCategory.DEFI
        "nft", "nftmarketplace", "nftlending" -> DAppCategory.NFT
        "gaming", "gamblefi" -> DAppCategory.GAMING
        "socialfi", "social" -> DAppCategory.SOCIAL
        "bridge", "crosschain" -> DAppCategory.BRIDGE
        "wallet" -> DAppCategory.WALLET
        "derivatives", "options", "perpetuals" -> DAppCategory.DEFI
        else -> DAppCategory.OTHER
    }
}

fun List<DAppDto>.toEntities(): List<DAppEntity> = map { it.toEntity() }
fun List<DAppEntity>.toDomainDApps(): List<DApp> = map { it.toDomain() }