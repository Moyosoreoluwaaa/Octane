package com.octane.data.di

import androidx.room.Room
import com.octane.core.network.ApiConfig
import com.octane.data.local.database.MIGRATION_1_2
import com.octane.data.local.database.OctaneDatabase
import com.octane.data.remote.api.*
import com.octane.data.repository.*
import com.octane.data.service.JupiterSwapService
import com.octane.domain.repository.*
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ✅ FIXED: Data module following repository patterns
 * Provides local database, remote APIs, and repository implementations
 */
val dataModule = module {

    // ===== LOCAL DATABASE (ROOM) =====
    single {
        Room.databaseBuilder(
            androidContext(),
            OctaneDatabase::class.java,
            OctaneDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(false)
            .build()
    }

    // ===== DAOs =====
    single { get<OctaneDatabase>().walletDao() }
    single { get<OctaneDatabase>().transactionDao() }
    single { get<OctaneDatabase>().assetDao() }
    single { get<OctaneDatabase>().contactDao() }
    single { get<OctaneDatabase>().approvalDao() }
    single { get<OctaneDatabase>().stakingDao() }
    single { get<OctaneDatabase>().discoverDao() }

    // ===== KTORFIT APIs =====

    // Solana RPC API
    single<SolanaRpcApi> {
        Ktorfit.Builder()
            .baseUrl(ApiConfig.Solana.MAINNET_PUBLIC)
            .httpClient(get<HttpClient>(named("SolanaRpcHttpClient")))
            .build()
            .createSolanaRpcApi()
    }

    // CoinGecko Price API
    single<PriceApi> {
        Ktorfit.Builder()
            .baseUrl(ApiConfig.COINGECKO_BASE_URL)
            .httpClient(get<HttpClient>(named("CoinGeckoHttpClient")))
            .build()
            .createPriceApi()
    }

    // Jupiter Swap Aggregator API
    single<SwapAggregatorApi> {
        Ktorfit.Builder()
            .baseUrl(ApiConfig.JUPITER_BASE_URL)
            .httpClient(get<HttpClient>(named("JupiterHttpClient")))
            .build()
            .createSwapAggregatorApi()
    }

    // ✅ CoinGecko Discover API
    single<DiscoverApi> {
        Ktorfit.Builder()
            .baseUrl(ApiConfig.COINGECKO_BASE_URL)
            .httpClient(get<HttpClient>(named("CoinGeckoHttpClient")))
            .build()
            .createDiscoverApi()
    }

    // ✅ DeFiLlama API
    single<DeFiLlamaApi> {
        Ktorfit.Builder()
            .baseUrl(ApiConfig.DRIFT_BASE_URL)
            .httpClient(get<HttpClient>(named("CoinGeckoHttpClient")))
            .build()
            .createDeFiLlamaApi()
    }

    // ✅ ADD THIS - Drift Protocol API
    single<DriftApi> {
        Ktorfit.Builder()
            .baseUrl("https://data.api.drift.trade/")
            .httpClient(get<HttpClient>(named("JupiterHttpClient")))
            .build()
            .createDriftApi()
    }

    // ===== SERVICES =====
    single {
        JupiterSwapService(
            jupiterApi = get()
        )
    }

    single<PriceRepository> {
        PriceRepositoryImpl(
            priceApi = get()
        )
    }

    // ===== REPOSITORIES =====
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

    single<ApprovalRepository> {
        ApprovalRepositoryImpl(
            approvalDao = get(),
            solanaRpcApi = get()
        )
    }
}