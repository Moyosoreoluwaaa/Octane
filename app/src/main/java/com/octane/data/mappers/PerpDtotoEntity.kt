package com.octane.data.mappers

import com.octane.data.local.database.entities.PerpEntity
import com.octane.data.remote.dto.drift.DriftContractDto
import com.octane.domain.models.Perp

/**
 * Mappers for Perp data transformation pipeline.
 *
 * Data Flow:
 * Drift API → DriftContractDto → PerpEntity (Database) → Perp (Domain)
 *
 * IMPORTANT:
 * - Drift API returns ALL contracts (PERP + SPOT)
 * - MUST filter by product_type == "PERP"
 * - All numeric fields are STRINGS - must parse to Double/Long
 */

// ==================== DTO → ENTITY ====================

/**
 * Convert Drift API DTO to Room Entity.
 *
 * CRITICAL MAPPINGS:
 * - tickerId → id (e.g., "SOL-PERP")
 * - lastPrice → markPrice (current futures price)
 * - indexPrice → indexPrice (spot price)
 * - quoteVolume → volume24h (USD volume, not base)
 * - fundingRate → fundingRate (8-hour rate as decimal)
 *
 * @return PerpEntity for database insertion
 */
fun DriftContractDto.toEntity(): PerpEntity {
    return PerpEntity(
        id = tickerId,  // "SOL-PERP", "BTC-PERP"
        symbol = tickerId,
        name = "$baseCurrency-$quoteCurrency Perpetual",
        logoUrl = null,  // TODO: Map from token metadata service

        // Prices (convert from String)
        indexPrice = indexPrice.toDoubleOrNull() ?: 0.0,
        markPrice = lastPrice.toDoubleOrNull() ?: 0.0,

        // Funding (convert from String)
        fundingRate = fundingRate.toDoubleOrNull() ?: 0.0,
        nextFundingTime = nextFundingRateTimestamp.toLongOrNull() ?: 0L,

        // Volume & OI (convert from String)
        openInterest = openInterest.toDoubleOrNull() ?: 0.0,
        volume24h = quoteVolume.toDoubleOrNull() ?: 0.0,  // Use quote volume (USD)

        // Price change (calculate from high/low)
        priceChange24h = calculatePriceChange24h(),

        // Metadata
        leverage = "20x",  // Drift default max leverage
        exchange = "Drift",
        lastUpdated = System.currentTimeMillis()
    )
}

/**
 * Convert list of DTOs to Entities.
 *
 * IMPORTANT: Filters out SPOT markets!
 * Only perpetual futures (product_type == "PERP") are converted.
 *
 * @return List of PerpEntity (database-ready)
 */
fun List<DriftContractDto>.toEntities(): List<PerpEntity> {
    return filter { it.isPerpetual }  // ✅ Filter PERP only
        .map { it.toEntity() }
}

// ==================== ENTITY → DOMAIN ====================

/**
 * Convert Room Entity to Domain Model.
 *
 * Domain model is used by ViewModels and UI.
 * Pure Kotlin, no Android dependencies.
 *
 * @return Perp domain model
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
        exchange = exchange,
        isLong = priceChange24h >= 0  // Green if positive change
    )
}

/**
 * Convert list of Entities to Domain Models.
 *
 * @return List of Perp domain models
 */
fun List<PerpEntity>.toDomainPerps(): List<Perp> = map { it.toDomain() }

// ==================== HELPERS ====================

/**
 * Calculate 24h price change percentage from high/low.
 *
 * Formula: ((current - midpoint) / midpoint) * 100
 * Midpoint = (high + low) / 2
 *
 * Example:
 * - High: $250, Low: $230, Current: $240
 * - Midpoint: $240
 * - Change: ((240 - 240) / 240) * 100 = 0%
 *
 * @return Price change percentage (e.g., 5.23 for +5.23%)
 */
private fun DriftContractDto.calculatePriceChange24h(): Double {
    val current = lastPrice.toDoubleOrNull() ?: return 0.0
    val highPrice = high.toDoubleOrNull() ?: return 0.0
    val lowPrice = low.toDoubleOrNull() ?: return 0.0

    if (lowPrice == 0.0) return 0.0

    // Use midpoint for better accuracy than just low
    val midpoint = (highPrice + lowPrice) / 2.0
    if (midpoint == 0.0) return 0.0

    return ((current - midpoint) / midpoint) * 100.0
}