package com.octane.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using Kotlin Serialization.
 * All routes are serializable for safe argument passing.
 */

@Serializable
sealed interface AppRoute {

    // Main Navigation Tabs
    @Serializable
    data object Home : AppRoute

    @Serializable
    data object Wallet : AppRoute

    @Serializable
    data object Swap : AppRoute

    @Serializable
    data object Search : AppRoute

    // Secondary Screens
    @Serializable
    data class TokenDetails(
        val tokenId: String,
        val symbol: String
    ) : AppRoute

    @Serializable
    data class PerpDetailRoute(
        val perpSymbol: String
    ) : AppRoute

    @Serializable
    data class DAppWebViewRoute(
        val url: String,
//        val title: String
    ) : AppRoute

    @Serializable
    data class Send(
        val tokenSymbol: String? = null,
        val prefilledAddress: String? = null
    ) : AppRoute

    @Serializable
    data class Receive(
        val tokenSymbol: String = "SOL"
    ) : AppRoute

    @Serializable
    data object ManageTokens : AppRoute

    @Serializable
    data object Wallets : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object Activity : AppRoute

    @Serializable
    data object Staking : AppRoute

    @Serializable
    data object Security : AppRoute

    @Serializable
    data class TransactionDetails(
        val txHash: String
    ) : AppRoute
}