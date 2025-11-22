package com.octane

import android.app.Application
import com.octane.core.di.coreModule
import com.octane.core.di.networkModule
import com.octane.data.di.preferencesModule
import com.octane.di.dataModule
import com.octane.di.repositoryModule
import com.octane.domain.di.domainModule
import com.octane.presentation.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class OctaneApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin DI
        startKoin {
            // Logging: ERROR in production, DEBUG in development
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)

            // Provide Android Context to modules
            androidContext(this@OctaneApplication)

            // Load modules IN ORDER (dependencies first)
            modules(
                // Layer 0: Core Infrastructure (no dependencies)
                coreModule,        // NetworkMonitor, LoadingState, Analytics, etc.
                networkModule,     // Retrofit, OkHttp, Ktor clients

                // Layer 1: Data Sources (depend on Core)
                dataModule,        // Room Database, API services
                preferencesModule, // ✅ UserPreferencesStore (DataStore)

                // Layer 2: Domain Logic (depend on Data)
                repositoryModule,  // Repository implementations
                domainModule,      // Use cases

                // Layer 3: Presentation (depend on Domain)
                viewModelModule    // ViewModels
            )
        }
    }
}