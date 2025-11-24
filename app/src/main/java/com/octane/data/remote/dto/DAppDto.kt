package com.octane.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DAppRadar/DeFiLlama API response for dApp data.
 * Endpoint: /dapps or /protocols
 */
@Serializable
data class DAppDto(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String,

    @SerialName("logo")
    val logo: String?,

    @SerialName("category")
    val category: String,

    @SerialName("url")
    val url: String,

    @SerialName("tvl")
    val tvl: Double? = null,

    @SerialName("volume_24h")
    val volume24h: Double? = null,

    @SerialName("users_24h")
    val users24h: Int? = null,

    @SerialName("chains")
    val chains: List<String>? = null,

    @SerialName("verified")
    val verified: Boolean? = true,

    @SerialName("rating")
    val rating: Double? = null,

    @SerialName("tags")
    val tags: List<String>? = null
)