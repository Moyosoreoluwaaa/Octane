package com.octane.browser

import android.content.ComponentCallbacks2
import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.octane.browser.design.BrowserTheme
import com.octane.browser.presentation.navigation.BrowserApp
import com.octane.browser.webview.WebViewDiagnostics
import timber.log.Timber

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Run diagnostics on app startup
        runDiagnostics()

        enableEdgeToEdge()
        setContent {
            BrowserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BrowserApp()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun runDiagnostics() {
        try {
            Timber.d("═══════════════════════════════════════")
            Timber.d("    OCTANE BROWSER STARTUP")
            Timber.d("═══════════════════════════════════════")

            WebViewDiagnostics.runStartupDiagnostics(this)
            WebViewDiagnostics.logGpuStatus()
            WebViewDiagnostics.detectCommonIssues(this)

            Timber.d("═══════════════════════════════════════")
            Timber.d("    STARTUP DIAGNOSTICS COMPLETE")
            Timber.d("═══════════════════════════════════════")
        } catch (e: Exception) {
            Timber.e(e, "Error running diagnostics")
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Timber.w("⚠️ LOW MEMORY WARNING - Aggressive cleanup")
        performMemoryCleanup(aggressive = true)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        when (level) {
            // App is running and memory is low
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                Timber.w("⚠️ Memory pressure: RUNNING_CRITICAL")
                performMemoryCleanup(aggressive = true)
            }
            TRIM_MEMORY_RUNNING_LOW -> {
                Timber.w("⚠️ Memory pressure: RUNNING_LOW")
                performMemoryCleanup(aggressive = false)
            }
            TRIM_MEMORY_RUNNING_MODERATE -> {
                Timber.d("ℹ️ Memory pressure: RUNNING_MODERATE")
                performMemoryCleanup(aggressive = false)
            }

            // App is in background
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                Timber.d("UI hidden - app moved to background")
                // Light cleanup when user switches apps
                performBackgroundCleanup()
            }

            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> {
                Timber.w("App backgrounded - TRIM_MEMORY_BACKGROUND")
                performBackgroundCleanup()
            }

            ComponentCallbacks2.TRIM_MEMORY_MODERATE -> {
                Timber.w("App backgrounded - TRIM_MEMORY_MODERATE")
                performMemoryCleanup(aggressive = false)
            }

            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                Timber.w("App backgrounded - TRIM_MEMORY_COMPLETE (near kill)")
                // System is about to kill background apps
                performMemoryCleanup(aggressive = true)
            }
        }
    }

    /**
     * Perform memory cleanup based on severity
     */
    private fun performMemoryCleanup(aggressive: Boolean) {
        try {
            if (aggressive) {
                Timber.d("🧹 Performing aggressive memory cleanup")

                // Clear WebView cache
                WebView(applicationContext).apply {
                    clearCache(true)
                    clearFormData()
                    clearHistory()
                    destroy()
                }

                // Clear cookies (keep login sessions)
                // CookieManager.getInstance().removeAllCookies(null)

                // Force garbage collection
                System.gc()

                Timber.d("✅ Aggressive cleanup complete")
            } else {
                Timber.d("🧹 Performing standard memory cleanup")

                // Clear WebView cache (keep recent)
                WebView(applicationContext).apply {
                    clearCache(false)  // Keep recent cache
                    destroy()
                }

                Timber.d("✅ Standard cleanup complete")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during memory cleanup")
        }
    }

    /**
     * Cleanup when app goes to background
     */
    private fun performBackgroundCleanup() {
        try {
            Timber.d("🧹 Background cleanup - preserving state")

            // Flush cookies to disk
            CookieManager.getInstance().flush()

            // Light memory cleanup
            System.gc()

            Timber.d("✅ Background cleanup complete")
        } catch (e: Exception) {
            Timber.e(e, "Error during background cleanup")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("MainActivity destroyed - final cleanup")
        performMemoryCleanup(aggressive = true)
    }
}