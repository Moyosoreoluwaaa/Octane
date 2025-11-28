package com.octane.browser.wallet_integration.models

data class TransactionRequest(
    val from: String,
    val to: String,
    val value: String,
    val data: String? = null,
    val gas: String? = null,
    val gasPrice: String? = null
)