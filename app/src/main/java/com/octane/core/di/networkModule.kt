package com.octane.core.di

import android.R.attr.level
import com.octane.core.blockchain.JupiterApiService
import com.octane.core.network.JupiterApiServiceImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Network Module for Jupiter API
 */

val networkModule = module {

    // HttpClient for Jupiter API (KMP-compatible)
    single {
        HttpClient(CIO) { // Use CIO instead of Android
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
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

            defaultRequest {
                url("https://quote-api.jup.ag/v6/")
            }
        }
    }

    // Jupiter API Service
    single<JupiterApiService> {
        JupiterApiServiceImpl(httpClient = get())
    }
}