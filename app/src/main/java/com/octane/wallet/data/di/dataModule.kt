package com.octane.wallet.data.di

import androidx.room.Room
import com.octane.wallet.core.network.ApiConfig
import com.octane.wallet.data.local.database.MIGRATION_1_2
import com.octane.wallet.data.local.database.OctaneDatabase
import com.octane.wallet.data.remote.api.DeFiLlamaApi
import com.octane.wallet.data.remote.api.DiscoverApi
import com.octane.wallet.data.remote.api.DriftApi
import com.octane.wallet.data.remote.api.PriceApi
import com.octane.wallet.data.remote.api.SolanaRpcApi
import com.octane.wallet.data.remote.api.SwapAggregatorApi
import com.octane.wallet.data.remote.api.createDeFiLlamaApi
import com.octane.wallet.data.remote.api.createDiscoverApi
import com.octane.wallet.data.remote.api.createDriftApi
import com.octane.wallet.data.remote.api.createPriceApi
import com.octane.wallet.data.remote.api.createSolanaRpcApi
import com.octane.wallet.data.remote.api.createSwapAggregatorApi
import com.octane.wallet.data.service.JupiterSwapService
import com.octane.wallet.data.service.TokenLogoResolver
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

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

    // CoinGecko Discover API
    single<DiscoverApi> {
        Ktorfit.Builder()
            .baseUrl(ApiConfig.COINGECKO_BASE_URL)
            .httpClient(get<HttpClient>(named("CoinGeckoHttpClient")))
            .build()
            .createDiscoverApi()
    }

    // ✅ FIXED: DeFiLlama API with correct base URL
    single<DeFiLlamaApi> {
        Ktorfit.Builder()
            .baseUrl(ApiConfig.DEFILLAMA_BASE_URL)
            .httpClient(get<HttpClient>(named("CoinGeckoHttpClient"))) // Reuse HTTP client
            .build()
            .createDeFiLlamaApi()
    }

    // Drift Protocol API
    single<DriftApi> {
        Ktorfit.Builder()
            .baseUrl(ApiConfig.DRIFT_URL)
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

    single {
        TokenLogoResolver(
            coinGeckoApi = get()
        )
    }
}