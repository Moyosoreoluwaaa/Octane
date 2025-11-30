package com.octane.browser.webview

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import timber.log.Timber

/**
 * ✅ MODERN: Dark Theme Manager for WebView
 *
 * Implements official Android dark theme support for WebView following:
 * https://developer.android.com/develop/ui/views/layout/webapps/dark-theme
 *
 * Features:
 * - Automatic theme detection (system dark mode)
 * - Algorithmic darkening for sites without prefers-color-scheme
 * - Web theme darkening for sites with dark mode support
 * - Backward compatibility (API 21+)
 *
 * Usage:
 * ```kotlin
 * WebViewThemeManager.setupTheme(webView, context)
 * ```
 */
object WebViewThemeManager {

    /**
     * Configure WebView dark theme based on system settings
     *
     * @param webView WebView instance to configure
     * @param context Context for accessing system configuration
     * @param forceDarkStrategy Optional: Override default dark strategy
     *
     * Call this in:
     * - WebView initialization (onCreate/factory)
     * - Configuration changes (onConfigurationChanged)
     */
    fun setupTheme(
        webView: WebView,
        context: Context,
        forceDarkStrategy: DarkStrategy = DarkStrategy.PREFER_WEB_THEME
    ) {
        val isDarkMode = isDarkMode(context)

        Timber.d("🎨 Setting up WebView theme - Dark Mode: $isDarkMode")

        when {
            // API 33+ (Android 13+): Use setAlgorithmicDarkeningAllowed
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                setupModernDarkTheme(webView, isDarkMode)
            }

            // API 29-32 (Android 10-12): Use FORCE_DARK
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                setupLegacyDarkTheme(webView, isDarkMode, forceDarkStrategy)
            }

            // API 21-28: No native dark theme support
            else -> {
                Timber.d("⚠️ Dark theme not supported on API ${Build.VERSION.SDK_INT}")
            }
        }
    }

    /**
     * Modern approach (Android 13+)
     * Uses setAlgorithmicDarkeningAllowed API
     */
    private fun setupModernDarkTheme(webView: WebView, isDarkMode: Boolean) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            try {
                // Enable algorithmic darkening when system is in dark mode
                // This only applies if web content doesn't use prefers-color-scheme
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(
                    webView.settings,
                    isDarkMode
                )
                Timber.d("✅ Algorithmic darkening: ${if (isDarkMode) "ENABLED" else "DISABLED"}")
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to set algorithmic darkening")
            }
        } else {
            Timber.w("⚠️ ALGORITHMIC_DARKENING not supported - falling back to FORCE_DARK")
            setupLegacyDarkTheme(webView, isDarkMode, DarkStrategy.PREFER_WEB_THEME)
        }
    }

    /**
     * Legacy approach (Android 10-12)
     * Uses FORCE_DARK API
     */
    private fun setupLegacyDarkTheme(
        webView: WebView,
        isDarkMode: Boolean,
        strategy: DarkStrategy
    ) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            try {
                val forceDarkMode = if (isDarkMode) {
                    WebSettingsCompat.FORCE_DARK_ON
                } else {
                    WebSettingsCompat.FORCE_DARK_OFF
                }

                WebSettingsCompat.setForceDark(webView.settings, forceDarkMode)
                Timber.d("✅ Force Dark: ${if (isDarkMode) "ON" else "OFF"}")

                // Set dark theme strategy (only applies when FORCE_DARK_ON)
                if (isDarkMode && WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
                    val strategyValue = when (strategy) {
                        DarkStrategy.PREFER_WEB_THEME ->
                            WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING
                        DarkStrategy.WEB_THEME_ONLY ->
                            WebSettingsCompat.DARK_STRATEGY_WEB_THEME_DARKENING_ONLY
                        DarkStrategy.USER_AGENT_ONLY ->
                            WebSettingsCompat.DARK_STRATEGY_USER_AGENT_DARKENING_ONLY
                    }

                    WebSettingsCompat.setForceDarkStrategy(webView.settings, strategyValue)
                    Timber.d("✅ Dark Strategy: $strategy")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to set force dark")
            }
        } else {
            Timber.w("⚠️ FORCE_DARK not supported on this device")
        }
    }

    /**
     * Check if system is in dark mode
     */
    fun isDarkMode(context: Context): Boolean {
        val nightModeFlag = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlag == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * Force theme update on configuration change
     * Call this in Activity.onConfigurationChanged()
     *
     * Note: In Compose, prefer using isSystemInDarkTheme() instead
     */
    fun onConfigurationChanged(webView: WebView, context: Context) {
        setupTheme(webView, context)
        Timber.d("🔄 Theme updated on configuration change")
    }

    /**
     * Create a configuration listener for non-Compose Activities
     *
     * Usage in Activity:
     * ```kotlin
     * private val themeListener = WebViewThemeManager.createConfigurationListener { isDark ->
     *     myWebView?.let { WebViewThemeManager.setupTheme(it, this) }
     * }
     *
     * override fun onConfigurationChanged(newConfig: Configuration) {
     *     super.onConfigurationChanged(newConfig)
     *     themeListener(newConfig)
     * }
     * ```
     */
    fun createConfigurationListener(
        onThemeChanged: (isDarkMode: Boolean) -> Unit
    ): (android.content.res.Configuration) -> Unit {
        return { config ->
            val isDark = (config.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES
            onThemeChanged(isDark)
            Timber.d("🔄 Configuration changed: Dark Mode = $isDark")
        }
    }

    /**
     * Dark theme strategies
     *
     * PREFER_WEB_THEME: Prefers sites with prefers-color-scheme, falls back to algorithmic
     * WEB_THEME_ONLY: Only applies dark theme if site supports prefers-color-scheme
     * USER_AGENT_ONLY: Always apply algorithmic darkening (ignore site's dark mode)
     */
    enum class DarkStrategy {
        /**
         * Default: Checks for <meta name="color-scheme"> tag
         * - If present: Uses site's prefers-color-scheme media queries
         * - If absent: Applies algorithmic darkening
         *
         * Recommended for general browsing
         */
        PREFER_WEB_THEME,

        /**
         * Only use web theme darkening (prefers-color-scheme)
         * Never apply algorithmic darkening
         *
         * Recommended for first-party content with custom dark themes
         */
        WEB_THEME_ONLY,

        /**
         * Always apply algorithmic darkening
         * Ignore prefers-color-scheme queries
         *
         * Use when sites don't properly implement dark mode
         */
        USER_AGENT_ONLY
    }

    /**
     * Diagnostics: Log current theme configuration
     */
    fun logThemeConfiguration(webView: WebView, context: Context) {
        Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Timber.d("      WEBVIEW THEME CONFIGURATION")
        Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Timber.d("System Dark Mode: ${isDarkMode(context)}")
        Timber.d("Android Version: ${Build.VERSION.SDK_INT}")

        Timber.d("Feature Support:")
        Timber.d("  ✓ ALGORITHMIC_DARKENING: ${WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)}")
        Timber.d("  ✓ FORCE_DARK: ${WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)}")
        Timber.d("  ✓ FORCE_DARK_STRATEGY: ${WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)}")

        Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}