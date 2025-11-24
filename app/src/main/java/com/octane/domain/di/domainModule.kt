package com.octane.domain.di

import com.octane.domain.usecases.transaction.EstimateTransactionFeeUseCase
import com.octane.domain.usecases.wallet.SetActiveWalletUseCase
import com.octane.domain.usecases.transaction.SwapTokensUseCase
import com.octane.domain.usecases.wallet.*
import com.octane.domain.usecases.asset.*
import com.octane.domain.usecases.discover.ObserveDAppsByCategoryUseCase
import com.octane.domain.usecases.discover.ObserveDAppsUseCase
import com.octane.domain.usecases.discover.ObservePerpsUseCase
import com.octane.domain.usecases.discover.ObserveTokensUseCase
import com.octane.domain.usecases.discover.ObserveTrendingTokensUseCase
import com.octane.domain.usecases.discover.RefreshDAppsUseCase
import com.octane.domain.usecases.discover.RefreshPerpsUseCase
import com.octane.domain.usecases.discover.RefreshTokensUseCase
import com.octane.domain.usecases.discover.SearchDAppsUseCase
import com.octane.domain.usecases.discover.SearchPerpsUseCase
import com.octane.domain.usecases.discover.SearchTokensUseCase
import com.octane.domain.usecases.transaction.*
import com.octane.domain.usecases.security.*
import com.octane.domain.usecases.network.*
import com.octane.domain.usecases.preference.ObserveCurrencyPreferenceUseCase
import com.octane.domain.usecases.preference.TogglePrivacyModeUseCase
import com.octane.domain.usecases.preference.UpdateCurrencyPreferenceUseCase
import com.octane.domain.usecases.staking.ClaimRewardsUseCase
import com.octane.domain.usecases.staking.ObserveStakingPositionsUseCase
import com.octane.domain.usecases.staking.StakeTokensUseCase
import com.octane.domain.usecases.staking.UnstakeTokensUseCase
import org.koin.dsl.module

val domainModule = module {

    // Wallet Use Cases
    factory { CreateWalletUseCase(get(), get(), get()) }
    factory { ImportWalletUseCase(get(), get(), get()) }
    factory { SetActiveWalletUseCase(get()) }
    factory { SwitchActiveWalletUseCase(get(), get()) }
    factory { ObserveWalletsUseCase(get()) }
    factory { DeleteWalletUseCase(get(), get()) }
    factory { ClaimRewardsUseCase(get(), get()) }
    factory { UpdateWalletMetadataUseCase(get()) }

    // Asset Use Cases
    factory { ObservePortfolioUseCase(get(), get()) }
    factory { RefreshAssetsUseCase(get(), get(), get()) }
    factory { ToggleAssetVisibilityUseCase(get()) }


    // Transaction Use Cases
    factory { SendSolUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { EstimateTransactionFeeUseCase(get(), get()) }
    factory { ObserveTransactionHistoryUseCase(get(), get()) }
    factory { MonitorPendingTransactionsUseCase(get(), get()) }
    factory { UpdateWalletUseCase(get()) }
    factory { ValidateSolanaAddressUseCase() }
    factory { SwapTokensUseCase(get(), get(), get(), get(), get()) }

    // PRICE USE CASES
    factory { ObserveRpcStatusUseCase(get()) }

    // Stacking Use Case
    factory { StakeTokensUseCase(get(), get(), get()) }
    factory { UnstakeTokensUseCase(get(), get()) }
    factory { ObserveStakingPositionsUseCase(get(), get()) }

    // Send Use Case
    factory { EstimateTransactionFeeUseCase(get(),get()) }


    // Security Use Cases
    factory { CheckBiometricAvailabilityUseCase(get()) }
    factory { AuthenticateWithBiometricsUseCase(get()) }

    // Network Use Cases
    factory { ObserveNetworkStatusUseCase(get()) }
    factory { SwitchRpcEndpointUseCase(get()) }

    // Preferences Use Cases
    factory { ObserveCurrencyPreferenceUseCase(get()) }
    factory { UpdateCurrencyPreferenceUseCase(get()) }
    factory { TogglePrivacyModeUseCase(get()) }

    // Approval use case
    factory { RevokeApprovalUseCase(get(), get()) }
    factory { ObserveApprovalsUseCase(get(), get()) }

    // Discover Use Cases - Tokens
    factory { ObserveTokensUseCase(get()) }
    factory { ObserveTrendingTokensUseCase(get()) }
    factory { SearchTokensUseCase(get()) }
    factory { RefreshTokensUseCase(get()) }

// Discover Use Cases - Perps
    factory { ObservePerpsUseCase(get()) }
    factory { SearchPerpsUseCase(get()) }
    factory { RefreshPerpsUseCase(get()) }

// Discover Use Cases - dApps
    factory { ObserveDAppsUseCase(get()) }
    factory { ObserveDAppsByCategoryUseCase(get()) }
    factory { SearchDAppsUseCase(get()) }
    factory { RefreshDAppsUseCase(get()) }
}