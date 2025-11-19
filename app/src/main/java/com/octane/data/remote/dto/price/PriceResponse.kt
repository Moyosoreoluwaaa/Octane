package com.octane.data.remote.dto.price

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PriceResponse(
    val usd: Double,
    @SerialName("usd_24h_change")
    val usd24hChange: Double? = null
)
