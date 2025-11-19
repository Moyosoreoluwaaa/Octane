// app/core/data/remote/api/PriceApi.kt

package com.octane.data.remote.api

import com.octane.data.remote.dto.price.PriceResponse
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import kotlin.collections.Map


/**
 * Price API (CoinGecko/CoinMarketCap).
 * Handles real-time price feeds for v0.9 portfolio and v1.2 P&L.
 */
interface PriceApi {

    @GET("simple/price")
    suspend fun getPrices(
        @Query("ids") coinIds: String,
        @Query("vs_currencies") currency: String = "usd",
        @Query("include_24hr_change") include24hrChange: Boolean = true
    ): Map<String, PriceResponse>
}