package com.octane.domain.di

import com.octane.domain.usecases.EstimateTransactionFeeUseCase
import com.octane.domain.usecases.SetActiveWalletUseCase
import com.octane.domain.usecases.SwapTokensUseCase
import com.octane.domain.usecases.wallet.*
import com.octane.domain.usecases.asset.*
import com.octane.domain.usecases.transaction.*
import com.octane.domain.usecases.security.*
import com.octane.domain.usecases.network.*
import com.octane.domain.usecases.preference.ObserveCurrencyPreferenceUseCase
import com.octane.domain.usecases.preference.TogglePrivacyModeUseCase
import com.octane.domain.usecases.preference.UpdateCurrencyPreferenceUseCase
import org.koin.dsl.module

val domainModule = module {

    // Wallet Use Cases
    factory { CreateWalletUseCase(get(), get(), get()) }
    factory { ImportWalletUseCase(get(), get(), get()) }
    factory { SetActiveWalletUseCase(get()) }
    factory { SwitchActiveWalletUseCase(get(), get()) }
    factory { ObserveWalletsUseCase(get()) }
    factory { DeleteWalletUseCase(get(), get()) }

    // Asset Use Cases
    factory { ObservePortfolioUseCase(get(), get()) }
    factory { RefreshAssetsUseCase(get(), get(), get()) }
    factory { ToggleAssetVisibilityUseCase(get()) }

    factory { SwapTokensUseCase(get(), get(), get(), get(), get()) }

    // Transaction Use Cases
    factory { SendSolUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { EstimateTransactionFeeUseCase(get(), get()) }
    factory { ObserveTransactionHistoryUseCase(get(), get()) }
    factory { MonitorPendingTransactionsUseCase(get(), get()) }

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
}