package com.octane.browser

import android.content.ComponentCallbacks2
import android.os.Build
import android.os.Bundle
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
import com.octane.ui.theme.OctaneTheme
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
                    // ✅ TESTING: Show browser instead of wallet
                    BrowserApp()

                    // 🔄 PRODUCTION: Uncomment this after testing
                    // val navController = rememberNavController()
                    // AppNavHost(
                    //     navController = navController,
                    //     modifier = Modifier
                    // )
                }
            }
        }
    }

    /**
     * Run comprehensive diagnostics on app startup
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun runDiagnostics() {
        try {
            Timber.d("═══════════════════════════════════════")
            Timber.d("    OCTANE BROWSER STARTUP")
            Timber.d("═══════════════════════════════════════")

            // Basic diagnostics
            WebViewDiagnostics.runStartupDiagnostics(this)

            // GPU status
            WebViewDiagnostics.logGpuStatus()

            // Check for common issues
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
        Timber.w("⚠️ LOW MEMORY WARNING")
        Timber.w("   Consider clearing cache or closing tabs")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        when (level) {
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                Timber.w("⚠️ Memory pressure: RUNNING_CRITICAL")
            }
            TRIM_MEMORY_RUNNING_LOW -> {
                Timber.w("⚠️ Memory pressure: RUNNING_LOW")
            }
            TRIM_MEMORY_RUNNING_MODERATE -> {
                Timber.d("ℹ️ Memory pressure: RUNNING_MODERATE")
            }

            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> {
                TODO()
            }

            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                TODO()
            }

            ComponentCallbacks2.TRIM_MEMORY_MODERATE -> {
                TODO()
            }

            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                TODO()
            }
        }
    }
}