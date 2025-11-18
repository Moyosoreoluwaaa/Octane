package com.octane

import android.app.Application
import com.octane.core.di.coreModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class OctaneApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin
        startKoin {
            androidContext(this@OctaneApplication)
            modules(
                coreModule
                // Add data, domain, presentation modules later
            )
        }
    }
}