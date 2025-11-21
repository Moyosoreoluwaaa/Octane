package com.octane.presentation.di

import com.octane.presentation.viewmodel.ActivityViewModel
import com.octane.presentation.viewmodel.BasePortfolioViewModel
import com.octane.presentation.viewmodel.BaseTransactionViewModel
import com.octane.presentation.viewmodel.BaseWalletViewModel
import com.octane.presentation.viewmodel.ReceiveViewModel
import com.octane.presentation.viewmodel.SendViewModel
import com.octane.presentation.viewmodel.SettingsViewModel
import com.octane.presentation.viewmodel.SwapViewModel
import com.octane.presentation.viewmodel.WalletsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    
    // ===== BASE VIEWMODELS (Shared State) =====
    
    viewModel {
        BaseWalletViewModel(
            observeWalletsUseCase = get(),
            createWalletUseCase = get(),
            importWalletUseCase = get(),
            deleteWalletUseCase = get(),
            setActiveWalletUseCase = get()
        )
    }
    
    viewModel {
        BaseTransactionViewModel(
            observeTransactionHistoryUseCase = get(),
            monitorPendingTransactionsUseCase = get(),
            walletRepository = get()
        )
    }
    
    viewModel {
        BasePortfolioViewModel(
            observePortfolioUseCase = get(),
            refreshAssetsUseCase = get(),
            toggleAssetVisibilityUseCase = get(),
            observeCurrencyUseCase = get(),
            networkMonitor = get()
        )
    }
    
    // ===== FEATURE VIEWMODELS =====
    
    viewModel {
        WalletsViewModel(
            baseWallet = get()
        )
    }
    
    viewModel {
        SwapViewModel(
            swapTokensUseCase = get(),
            estimateFeeUseCase = get(),
            authenticateWithBiometricsUseCase = get(),
            jupiterApi = get(),
            basePortfolio = get(),
            baseWallet = get()
        )
    }
    
    viewModel {
        SettingsViewModel(
            updateCurrencyUseCase = get(),
            togglePrivacyModeUseCase = get(),
            switchRpcEndpointUseCase = get(),
            observeNetworkStatusUseCase = get(),
            observeCurrencyUseCase = get(),
            checkBiometricUseCase = get(),
            userPreferencesStore = get()
        )
    }
    
    viewModel {
        SendViewModel(
            estimateFeeUseCase = get(),
            sendTokenUseCase = get(),
            authenticateWithBiometricsUseCase = get(),
            validateSolanaAddressUseCase = get(),
            basePortfolio = get(),
            baseWallet = get()
        )
    }
    
    viewModel {
        ReceiveViewModel(
            baseWallet = get(),
            basePortfolio = get(),
            qrCodeGenerator = get()
        )
    }
    
    viewModel {
        ActivityViewModel(
            baseTransaction = get(),
            observeTransactionHistoryUseCase = get()
        )
    }
}