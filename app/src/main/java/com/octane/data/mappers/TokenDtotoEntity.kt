package com.octane.data.mappers

import TokenDto
import com.octane.data.local.database.entities.TokenEntity
import com.octane.domain.models.Token


/**
 * DTO → Entity (API → Database)
 */
fun TokenDto.toEntity(): TokenEntity {
    return TokenEntity(
        id = id,
        symbol = symbol.uppercase(),
        name = name,
        logoUrl = image,
        currentPrice = currentPrice,
        priceChange24h = priceChange24h ?: 0.0,
        marketCap = marketCap,
        volume24h = totalVolume,
        rank = marketCapRank ?: Int.MAX_VALUE,
        isVerified = true, // Assume CoinGecko tokens are verified
        tags = "", // TODO: Add tags if available in future API versions
        mintAddress = platforms?.get("solana"), // Extract Solana mint address
        lastUpdated = System.currentTimeMillis()
    )
}

/**
 * Entity → Domain (Database → Business Logic)
 */
fun TokenEntity.toDomain(): Token {
    return Token(
        id = id,
        symbol = symbol,
        name = name,
        logoUrl = logoUrl,
        currentPrice = currentPrice,
        priceChange24h = priceChange24h,
        marketCap = marketCap,
        volume24h = volume24h,
        rank = rank,
        isVerified = isVerified,
        tags = tags.split(",").filter { it.isNotBlank() },
        mintAddress = mintAddress
    )
}

/**
 * List extensions for batch mapping.
 */
fun List<TokenDto>.toEntities(): List<TokenEntity> = map { it.toEntity() }
fun List<TokenEntity>.toDomainTokens(): List<Token> = map { it.toDomain() }
