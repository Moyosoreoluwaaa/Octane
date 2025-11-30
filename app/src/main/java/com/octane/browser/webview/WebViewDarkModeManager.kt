package com.octane.browser.webview

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.octane.browser.domain.models.Theme
import timber.log.Timber

/**
 * ✅ FIXED: WebView Dark Mode Manager that integrates with BrowserTheme
 * 
 * Key fixes:
 * 1. Respects Theme.LIGHT, Theme.DARK, Theme.SYSTEM from your ThemeManager
 * 2. Doesn't break dynamic colors
 * 3. Properly detects system theme
 * 4. Works with your existing BrowserTheme composable
 */
object WebViewDarkModeManager {

    /**
     * ✅ NEW: Setup dark mode based on Theme enum
     * Use this from Compose with your ThemeManager
     */
    fun setupDarkMode(webView: WebView, currentTheme: Theme) {
        val isDarkMode = when (currentTheme) {
            Theme.LIGHT -> false
            Theme.DARK -> true
            Theme.SYSTEM -> isSystemInDarkMode(webView)
        }
        
        applyDarkMode(webView.settings, isDarkMode)
    }

    /**
     * Core dark mode application logic
     */
    private fun applyDarkMode(settings: WebSettings, isDarkMode: Boolean) {
        Timber.d("🌙 Applying WebView dark mode: isDarkMode=$isDarkMode, SDK=${Build.VERSION.SDK_INT}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            setupDarkModeForAndroid13Plus(settings, isDarkMode)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12 (API 29-32)
            setupDarkModeForAndroid10To12(settings, isDarkMode)
        } else {
            Timber.w("⚠️ Dark mode not supported below Android 10 (API 29)")
        }
    }

    /**
     * Android 13+ (API 33+) approach
     */
    @SuppressLint("RequiresFeature")
    private fun setupDarkModeForAndroid13Plus(settings: WebSettings, isDarkMode: Boolean) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            Timber.w("⚠️ ALGORITHMIC_DARKENING not supported on this device")
            return
        }

        try {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, isDarkMode)
            Timber.d("✅ Algorithmic darkening: ${if (isDarkMode) "ENABLED" else "DISABLED"}")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to set algorithmic darkening")
        }
    }

    /**
     * Android 10-12 (API 29-32) approach
     */
    @SuppressLint("RequiresFeature")
    private fun setupDarkModeForAndroid10To12(settings: WebSettings, isDarkMode: Boolean) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            Timber.w("⚠️ FORCE_DARK not supported on this device")
            return
        }

        try {
            if (isDarkMode) {
                @Suppress("DEPRECATION")
                WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON)
                Timber.d("✅ Force Dark: ON")
                setupDarkModeStrategy(settings)
            } else {
                @Suppress("DEPRECATION")
                WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_OFF)
                Timber.d("✅ Force Dark: OFF")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to set force dark")
        }
    }

    /**
     * Configure darkening strategy
     */
    private fun setupDarkModeStrategy(settings: WebSettings) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
            return
        }

        try {
            @Suppress("DEPRECATION")
            WebSettingsCompat.setForceDarkStrategy(
                settings,
                WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING
            )
            Timber.d("✅ Dark Strategy: PREFER_WEB_THEME")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to set dark strategy")
        }
    }

    /**
     * Check if system is in dark mode
     */
    private fun isSystemInDarkMode(webView: WebView): Boolean {
        val nightModeFlags = webView.resources.configuration.uiMode and 
                Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * ✅ COMPOSABLE HELPER: Get current dark mode state
     * Use this inside Compose to pass to WebView setup
     */
    @Composable
    fun rememberIsDarkMode(currentTheme: Theme): Boolean {
        val configuration = LocalConfiguration.current
        
        return remember(currentTheme, configuration) {
            when (currentTheme) {
                Theme.LIGHT -> false
                Theme.DARK -> true
                Theme.SYSTEM -> {
                    val uiMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    uiMode == Configuration.UI_MODE_NIGHT_YES
                }
            }
        }
    }
}