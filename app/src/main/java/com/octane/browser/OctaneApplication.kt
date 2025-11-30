package com.octane.browser

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.octane.BuildConfig
import com.octane.browser.di.browserModule
import com.octane.browser.di.quickAccessModule
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.util.concurrent.TimeUnit

class OctaneBrowserApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin DI
        startKoin {
            // Logging: ERROR in production, DEBUG in development
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)

            // Provide Android Context to modules
            androidContext(this@OctaneBrowserApplication)

            // Load modules IN ORDER (dependencies first)
            modules(
                // ✅ Layer 4: Browser Module (self-contained)
                browserModule,      // Browser feature with all dependencies
                quickAccessModule
            )
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(
                        context.cacheDir.resolve("image_cache").toOkioPath()
                    )
                    .maxSizeBytes(50 * 1024 * 1024)
                    .build()
            }
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = {
                            OkHttpClient.Builder()
                                .connectTimeout(30, TimeUnit.SECONDS)
                                .readTimeout(30, TimeUnit.SECONDS)
                                .writeTimeout(30, TimeUnit.SECONDS)
                                .build()
                        }
                    )
                )
            }
            .build()
    }
}