package com.octane.data.di

import androidx.room.Room
import com.octane.core.network.ApiConfig
import com.octane.data.local.database.OctaneDatabase
import com.octane.data.remote.api.PriceApi
import com.octane.data.remote.api.SolanaRpcApi
import com.octane.data.remote.api.SwapAggregatorApi
import com.octane.data.remote.api.createPriceApi
import com.octane.data.remote.api.createSolanaRpcApi
import com.octane.data.remote.api.createSwapAggregatorApi
import com.octane.data.repository.AssetRepositoryImpl
import com.octane.data.repository.ApprovalRepositoryImpl
import com.octane.data.repository.TransactionRepositoryImpl
import com.octane.data.repository.WalletRepositoryImpl
import com.octane.data.repository.PriceRepositoryImpl
import com.octane.data.repository.PriceRepository
import com.octane.data.service.JupiterSwapService
import com.octane.domain.repository.ApprovalRepository
import com.octane.domain.repository.AssetRepository
import com.octane.domain.repository.TransactionRepository
import com.octane.domain.repository.WalletRepository
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ✅ FIXED: Data module following repository patterns
 * Provides local database, remote APIs, and repository implementations
 */
val dataModule = module {

    // ===== LOCAL DATABASE (ROOM) =====
    /**
     * OctaneDatabase instance
     * Uses destructive migration for v0.x (replace with proper migrations in production)
     */
    single {
        Room.databaseBuilder(
            androidContext(),
            OctaneDatabase::class.java,
            OctaneDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    // ===== DAOs =====
    // Each DAO exposed separately for granular injection
    single { get<OctaneDatabase>().walletDao() }
    single { get<OctaneDatabase>().transactionDao() }
    single { get<OctaneDatabase>().assetDao() }
    single { get<OctaneDatabase>().contactDao() }
    single { get<OctaneDatabase>().approvalDao() }
    single { get<OctaneDatabase>().stakingDao() }

    // ===== KTORFIT (Retrofit-like for Ktor) =====
    /**
     * ✅ FIXED: Use named HttpClients from networkModule
     * Avoids creating duplicate clients
     */

    // Solana RPC API
    single<SolanaRpcApi> {
        val builder = Ktorfit.Builder()
            .baseUrl(ApiConfig.Solana.MAINNET_PUBLIC)
        builder.httpClient(get<io.ktor.client.HttpClient>(named("SolanaRpcHttpClient")))
        builder.build().createSolanaRpcApi()
    }

    // CoinGecko Price API
    single<PriceApi> {
        val builder = Ktorfit.Builder()
            .baseUrl(ApiConfig.COINGECKO_BASE_URL)
        builder.httpClient(get<io.ktor.client.HttpClient>(named("CoinGeckoHttpClient")))
        builder.build().createPriceApi()
    }

    // Jupiter Swap Aggregator API
    single<SwapAggregatorApi> {
        val builder = Ktorfit.Builder()
            .baseUrl(ApiConfig.JUPITER_BASE_URL)
        builder.httpClient(get<io.ktor.client.HttpClient>(named("JupiterHttpClient")))
        builder.build().createSwapAggregatorApi()
    }

    // ===== SERVICES (Domain Logic) =====
    /**
     * Jupiter Swap Service
     * Wraps SwapAggregatorApi with domain logic
     */
    single {
        JupiterSwapService(
            jupiterApi = get()
        )
    }

    /**
     * Price Repository
     * Maps token symbols to CoinGecko IDs
     */
    single<PriceRepository> {
        PriceRepositoryImpl(
            priceApi = get()
        )
    }

    // ===== REPOSITORIES =====
    /**
     * ✅ PATTERN: Repositories follow offline-first pattern
     * - Database is single source of truth
     * - Network updates database
     * - UI observes database Flow
     */

    // Wallet Repository
    single<WalletRepository> {
        WalletRepositoryImpl(
            walletDao = get()
        )
    }

    // Asset Repository (Portfolio)
    single<AssetRepository> {
        AssetRepositoryImpl(
            assetDao = get(),
            solanaRpcApi = get(),
            priceApi = get(),
            networkMonitor = get() // From coreModule
        )
    }

    // Transaction Repository
    single<TransactionRepository> {
        TransactionRepositoryImpl(
            transactionDao = get(),
            solanaRpcApi = get(),
            networkMonitor = get() // From coreModule
        )
    }

    // Approval Repository (V1.7 - dApp connections)
    single<ApprovalRepository> {
        ApprovalRepositoryImpl(
            approvalDao = get(),
            solanaRpcApi = get()
        )
    }
}