package com.octane.core.di

import com.octane.core.blockchain.*
import com.octane.core.monitoring.*
import com.octane.core.network.*
import com.octane.core.security.*
import com.octane.data.repository.NetworkMonitorImpl
import com.octane.data.repository.SolanaKeyGeneratorImpl
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ✅ FIXED: Core module following Phase 0 patterns
 * Provides cross-cutting utilities used by ALL layers
 */
val coreModule = module {

    // ===== DISPATCHERS =====
    // Named qualifiers for injection clarity
    single(named("IO")) { Dispatchers.IO }
    single(named("Main")) { Dispatchers.Main }
    single(named("Default")) { Dispatchers.Default }

    // ===== NETWORK MONITORING =====
    // ✅ FIXED: Use proper implementation class
    single<NetworkMonitor> {
        NetworkMonitorImpl(androidContext())
    }

    // Solana-specific network monitor (depends on base NetworkMonitor)
    single {
        SolanaNetworkMonitor(get())
    }

    // ===== BLOCKCHAIN CORE =====
    // Key generation
    single<SolanaKeyGenerator> {
        SolanaKeyGeneratorImpl()
    }

    // RPC management with health checks
    single {
        SolanaRpcManager(
            networkMonitor = get(),
            solanaRpcApi = get() // Will be provided by dataModule
        )
    }

    // Transaction utilities
    single { GasFeeEstimator() }
    single<TransactionSimulator> { TransactionSimulatorImpl() }
    single { TransactionBuilder() }
    single { SolanaWalletAdapter(get(), get(), get(), get(), get(), get()) }

    // ===== SECURITY =====
    // Keystore for secure key storage
    single {
        KeystoreManager(androidContext())
    }
    single { SolanaWalletAdapter(get(), get(), get(), get(), get(), get()) }

    // Biometric authentication
    single {
        BiometricManager(androidContext())
    }

    // Transaction security
    single<MaliciousSignatureDetector> {
        MaliciousSignatureDetectorImpl()
    }

    // Phishing protection
    single {
        PhishingBlocklist()
    }

    // ===== MONITORING & ANALYTICS =====
    /**
     * ✅ Use FakeAnalyticsLogger for debug, Firebase for release
     * Switch based on BuildConfig
     */
    single<AnalyticsLogger> {
        // TODO: Replace with actual BuildConfig check
        // if (BuildConfig.DEBUG) {
        FakeAnalyticsLogger()
        // } else {
        //     FirebaseAnalyticsLogger(get())
        // }
    }

    // Performance tracking
    single {
        PerformanceTracker(analyticsLogger = get())
    }
}