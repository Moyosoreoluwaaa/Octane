package com.octane.domain.usecase.asset

import com.octane.core.util.LoadingState
import com.octane.domain.models.Asset
import com.octane.domain.repository.AssetRepository
import com.octane.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Observes portfolio assets for a wallet.
 *
 * Features:
 * - Real-time updates via Flow
 * - Sorted by USD value (highest first)
 * - Filters hidden assets
 * - Calculates total portfolio value
 */
class ObservePortfolioUseCase(
    private val assetRepository: AssetRepository,
    private val walletRepository: WalletRepository
) {
    operator fun invoke(): Flow<LoadingState<PortfolioState>> {
        return walletRepository.observeActiveWallet()
            .combine(assetRepository.observeAssets()) { wallet, assets ->
                if (wallet == null) {
                    return@combine LoadingState.Error(
                        IllegalStateException("No active wallet")
                    )
                }

                val visibleAssets = assets.filter { !it.isHidden }
                val totalValue = visibleAssets.sumOf { it.valueUsd ?: 0.0 }
                val total24hChange = calculatePortfolioChange(visibleAssets)

                LoadingState.Success(
                    PortfolioState(
                        assets = visibleAssets.sortedByDescending { it.valueUsd },
                        totalValueUsd = totalValue,
                        change24hPercent = total24hChange
                    )
                )
            }
    }

    private fun calculatePortfolioChange(assets: List<Asset>): Double {
        val totalValue = assets.sumOf { it.valueUsd ?: 0.0 }
        if (totalValue == 0.0) return 0.0

        val totalChange = assets.sumOf { asset ->
            val value = asset.valueUsd ?: 0.0
            val change = asset.priceChange24h ?: 0.0
            value * (change / 100.0)
        }

        return (totalChange / totalValue) * 100.0
    }
}

data class PortfolioState(
    val assets: List<Asset>,
    val totalValueUsd: Double,
    val change24hPercent: Double
)

