package com.octane

import android.app.Application
import com.octane.core.di.coreModule
import com.octane.di.dataModule
import com.octane.di.repositoryModule
import com.octane.domain.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class OctaneApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin DI
        startKoin {
            androidLogger(Level.ERROR) // Only show errors in production
            androidContext(this@OctaneApplication)
            modules(
                coreModule,        // Core utilities (NetworkMonitor, LoadingState, etc.)
                dataModule,        // Database, DataStore, API clients
                repositoryModule,   // Repository implementations
                domainModule,      // Domain layer (use cases, repositories)
                // TODO: Add presentationModule (ViewModels)
            )
        }
    }
}

