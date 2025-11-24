package com.octane.data.mappers

import com.octane.data.local.database.entities.PerpEntity
import com.octane.data.remote.dto.PerpDto
import com.octane.domain.models.Perp


/**
 * DTO → Entity
 */
fun PerpDto.toEntity(): PerpEntity {
    return PerpEntity(
        id = symbol,
        symbol = symbol,
        name = "$baseAsset-$quoteAsset Perpetual",
        logoUrl = null, // TODO: Map from token metadata
        indexPrice = indexPrice,
        markPrice = markPrice,
        fundingRate = fundingRate,
        nextFundingTime = nextFundingTime,
        openInterest = openInterest,
        volume24h = volume24h,
        priceChange24h = priceChange24h,
        leverage = maxLeverage ?: "20x",
        exchange = exchange ?: "Jupiter",
        lastUpdated = System.currentTimeMillis()
    )
}

/**
 * Entity → Domain
 */
fun PerpEntity.toDomain(): Perp {
    return Perp(
        id = id,
        symbol = symbol,
        name = name,
        logoUrl = logoUrl,
        indexPrice = indexPrice,
        markPrice = markPrice,
        fundingRate = fundingRate,
        nextFundingTime = nextFundingTime,
        openInterest = openInterest,
        volume24h = volume24h,
        priceChange24h = priceChange24h,
        leverage = leverage,
        exchange = exchange
    )
}

fun List<PerpDto>.toEntities(): List<PerpEntity> = map { it.toEntity() }
fun List<PerpEntity>.toDomainPerps(): List<Perp> = map { it.toDomain() }
