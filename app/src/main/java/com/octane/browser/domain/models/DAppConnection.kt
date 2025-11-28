package com.octane.browser.domain.models

data class DAppConnection(
    val id: String,
    val domain: String,
    val chainId: String,
    val connectedAddress: String,
    val connectedAt: Long,
    val lastUsedAt: Long,
    val permissions: List<String> = emptyList()
)