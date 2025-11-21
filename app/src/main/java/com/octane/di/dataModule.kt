package com.octane.di

import androidx.room.Room
import com.octane.data.local.database.OctaneDatabase
import com.octane.data.local.datastore.UserPreferencesStore
import com.octane.data.remote.api.PriceApi
import com.octane.data.remote.api.SolanaRpcApi
import com.octane.data.remote.api.SwapAggregatorApi
import com.octane.data.remote.api.createPriceApi
import com.octane.data.remote.api.createSolanaRpcApi
import com.octane.data.remote.api.createSwapAggregatorApi
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module for Data layer dependencies.
 */
val dataModule = module {

    // ===== LOCAL DATABASE =====
    single {
        Room.databaseBuilder(
            androidContext(),
            OctaneDatabase::class.java,
            OctaneDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true) // v0.x only; use proper migrations in production
            .build()
    }

    single { get<OctaneDatabase>().walletDao() }
    single { get<OctaneDatabase>().transactionDao() }
    single { get<OctaneDatabase>().assetDao() }
    single { get<OctaneDatabase>().contactDao() }
    single { get<OctaneDatabase>().approvalDao() }
    single { get<OctaneDatabase>().stakingDao() }

    // ===== HTTP CLIENT =====
    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                })
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
            }
        }
    }

    // ===== KTORFIT (Retrofit-like for Ktor) =====

    // Solana RPC - Using Triton One (working endpoint)
    single<SolanaRpcApi> {
        Ktorfit.Builder()
            .baseUrl("https://fra130.nodes.rpcpool.com/") // TODO: Use dynamic RPC from settings
            .httpClient(get<HttpClient>())
            .build()
            .createSolanaRpcApi()
    }

    // Price API (CoinGecko)
    single<PriceApi> {
        Ktorfit.Builder()
            .baseUrl("https://api.coingecko.com/api/v3/")
            .httpClient(get<HttpClient>())
            .build()
            .createPriceApi()
    }

    // Jupiter Swap Aggregator
    single<SwapAggregatorApi> {
        Ktorfit.Builder()
            .baseUrl("https://quote-api.jup.ag/v6/")
            .httpClient(get<HttpClient>())
            .build()
            .createSwapAggregatorApi()
    }
}