// data/remote/api/DiscoverApi.kt
package com.octane.data.remote.api


import TokenDto
import com.octane.data.remote.dto.DAppDto
import com.octane.data.remote.dto.PerpDto
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import java.util.List

/**
 * Discover API - aggregates tokens, perps, and dApps.
 * Uses multiple data sources:
 * - CoinGecko: Token prices and market data
 * - Jupiter/Drift: Perps data
 * - DeFiLlama: dApp TVL and metrics
 */
interface DiscoverApi {

    // ==================== TOKENS ====================

    /**
     * Get token market data (CoinGecko).
     * Endpoint: /coins/markets
     * 
     * @param vsCurrency Currency for pricing (default: USD)
     * @param order Sort order (market_cap_desc, volume_desc)
     * @param perPage Results per page (max: 250)
     * @param page Page number
     * @param sparkline Include 7-day sparkline
     * @param priceChangePercentage Time periods for price change
     */
    @GET("coins/markets")
    suspend fun getTokens(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false,
        @Query("price_change_percentage") priceChangePercentage: String = "24h"
    ): List<TokenDto>

    /**
     * Search tokens by query.
     * Endpoint: /search
     */
    @GET("search")
    suspend fun searchTokens(
        @Query("query") query: String
    ): TokenSearchResponse

    // ==================== PERPS ====================

    /**
     * Get perpetual futures data.
     * NOTE: This is a PLACEHOLDER - Jupiter doesn't have a public Perps API yet.
     * 
     * Options for implementation:
     * 1. Use Drift Protocol API (https://docs.drift.trade/)
     * 2. Use Mango Markets API (https://docs.mango.markets/)
     * 3. Aggregate from multiple DEXs via Jupiter
     * 
     * For now, returning mock data structure.
     */
    @GET("perps/markets")
    suspend fun getPerps(): List<PerpDto>

    // ==================== DAPPS ====================

    /**
     * Get dApp data (DeFiLlama).
     * Endpoint: /protocols
     * 
     * NOTE: DeFiLlama uses different base URL.
     * Create separate Ktorfit instance for this.
     */
    @GET("protocols")
    suspend fun getDApps(): List<DAppDto>

    /**
     * Get dApps by category.
     */
    @GET("protocols")
    suspend fun getDAppsByCategory(
        @Query("category") category: String
    ): List<DAppDto>
}

/**
 * Token search response wrapper.
 */
@kotlinx.serialization.Serializable
data class TokenSearchResponse(
    val coins: List<TokenSearchResult>
)

@kotlinx.serialization.Serializable
data class TokenSearchResult(
    val id: String,
    val name: String,
    val symbol: String,
    val thumb: String?,
    val market_cap_rank: Int?
)
