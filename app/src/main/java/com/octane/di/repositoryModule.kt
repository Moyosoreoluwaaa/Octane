package com.octane.di

import com.octane.data.repository.AssetRepositoryImpl
import com.octane.data.repository.TransactionRepositoryImpl
import com.octane.data.repository.WalletRepositoryImpl
import com.octane.domain.repository.AssetRepository
import com.octane.domain.repository.TransactionRepository
import com.octane.domain.repository.WalletRepository
import org.koin.dsl.module

/**
 * Koin module for Repository implementations.
 */
val repositoryModule = module {

    single<WalletRepository> {
        WalletRepositoryImpl(
            walletDao = get()
        )
    }

    single<AssetRepository> {
        AssetRepositoryImpl(
            assetDao = get(),
            solanaRpcApi = get(),
            priceApi = get(),
            networkMonitor = get()
        )
    }

    single<TransactionRepository> {
        TransactionRepositoryImpl(
            transactionDao = get(),
            solanaRpcApi = get(),
            networkMonitor = get()
        )
    }
}