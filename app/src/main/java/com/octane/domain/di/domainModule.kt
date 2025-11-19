package com.octane.domain.di

import com.octane.domain.usecase.wallet.*
import com.octane.domain.usecase.asset.*
import com.octane.domain.usecase.transaction.*
import com.octane.domain.usecase.security.*
import com.octane.domain.usecase.network.*
import com.octane.domain.usecase.preference.ObserveCurrencyPreferenceUseCase
import com.octane.domain.usecase.preference.TogglePrivacyModeUseCase
import com.octane.domain.usecase.preference.UpdateCurrencyPreferenceUseCase
import org.koin.dsl.module

val domainModule = module {

    // Wallet Use Cases
    factory { CreateWalletUseCase(get(), get(), get()) }
    factory { ImportWalletUseCase(get(), get(), get()) }
    factory { SwitchActiveWalletUseCase(get(), get()) }
    factory { ObserveWalletsUseCase(get()) }
    factory { DeleteWalletUseCase(get(), get()) }

    // Asset Use Cases
    factory { ObservePortfolioUseCase(get(), get()) }
    factory { RefreshAssetsUseCase(get(), get(), get()) }
    factory { ToggleAssetVisibilityUseCase(get()) }

    // Transaction Use Cases
    factory { SendSolUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
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