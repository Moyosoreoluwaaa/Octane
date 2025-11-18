package com.octane.core.di

import com.octane.core.blockchain.*
import com.octane.core.monitoring.*
import com.octane.core.network.*
import com.octane.core.security.*
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin DI module for Core layer.
 * Provides all core infrastructure components.
 */
val coreModule = module {

    // Dispatchers
    single(named("IO")) { Dispatchers.IO }
    single(named("Main")) { Dispatchers.Main }
    single(named("Default")) { Dispatchers.Default }

    // Network
    single<NetworkMonitor> { NetworkMonitorImpl(androidContext()) }
    single { SolanaNetworkMonitor(get()) }

    // Blockchain
    single { SolanaRpcManager(get()) }
    single { GasFeeEstimator() }
    single<TransactionSimulator> { TransactionSimulatorImpl() }
    single { TransactionBuilder() }

    // Security
    single { KeystoreManager(androidContext()) }
    single { BiometricManager(androidContext()) }
    single<MaliciousSignatureDetector> { MaliciousSignatureDetectorImpl() }
    single { PhishingBlocklist() }

    // Monitoring
    single<AnalyticsLogger> {
        // TODO: Replace with Firebase in production
        FakeAnalyticsLogger()
    }
    single { PerformanceTracker(get()) }
}