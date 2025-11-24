package com.octane.data.remote.api

import com.octane.data.remote.dto.DAppDto
import de.jensklingenberg.ktorfit.http.GET

/**
 * DeFiLlama API for dApp TVL and metrics.
 * Base URL: https://api.llama.fi
 */
interface DeFiLlamaApi {

    /**
     * Get all protocols (dApps).
     * Returns TVL, volume, and chain data.
     */
    @GET("protocols")
    suspend fun getProtocols(): List<DAppDto>

    /**
     * Get protocol by slug.
     */
    @GET("protocol/{slug}")
    suspend fun getProtocol(@de.jensklingenberg.ktorfit.http.Path("slug") slug: String): DAppDto
}