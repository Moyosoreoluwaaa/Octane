package com.octane.wallet.core.di

import com.octane.wallet.core.blockchain.JupiterApiService
import com.octane.wallet.core.network.JupiterApiServiceImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Network Module - provides HTTP clients for all APIs
 */
val networkModule = module {

    // ===== SHARED JSON CONFIGURATION =====
    single {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    // ===== SOLANA RPC HTTP CLIENT =====
    single(named("SolanaRpcHttpClient")) {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(get()) // Use shared Json configuration
            }

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }

            // No defaultRequest - baseUrl set by Ktorfit
        }
    }

    // ===== COINGECKO HTTP CLIENT =====
    single(named("CoinGeckoHttpClient")) {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(get()) // Use shared Json configuration
            }

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 15_000
            }

            // No defaultRequest - baseUrl set by Ktorfit
        }
    }

    // ===== JUPITER HTTP CLIENT =====
    single(named("JupiterHttpClient")) {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(get()) // Use shared Json configuration
            }

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }

            // No defaultRequest - baseUrl set by Ktorfit
        }
    }

    // ===== LEGACY JUPITER API SERVICE =====
    // Keep for backward compatibility if needed
    single<JupiterApiService> {
        JupiterApiServiceImpl(httpClient = get(named("JupiterHttpClient")))
    }
}