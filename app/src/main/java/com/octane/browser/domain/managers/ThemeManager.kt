package com.octane.browser.domain.managers

import com.octane.browser.domain.models.Theme
import com.octane.browser.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

/**
 * ThemeManager - Reactive theme state management.
 *
 * Purpose:
 * - Exposes current theme as StateFlow (UI observes)
 * - Automatically syncs with DataStore changes
 * - Provides convenient access to theme state
 *
 * Lifecycle:
 * - Singleton - lives entire app lifetime
 * - Scope: Application scope (injected)
 * - Started: Eagerly (loads theme immediately)
 *
 * Usage:
 * ```
 * @Composable
 * fun MyApp() {
 *     val themeManager: ThemeManager = koinInject()
 *     val theme by themeManager.currentTheme.collectAsState()
 *     val useDynamic by themeManager.useDynamicColors.collectAsState()
 *
 *     BrowserTheme(theme, useDynamic) { /* content */ }
 * }
 * ```
 */
class ThemeManager(
    private val settingsRepository: SettingsRepository,
    scope: CoroutineScope
) {

    /**
     * Current theme preference.
     * Emits: LIGHT, DARK, or SYSTEM
     *
     * Hot Flow: Always has a value (default: SYSTEM)
     * Shared: Single source for all observers
     * Lifecycle: Stops 5s after last collector unsubscribes
     */
    val currentTheme: StateFlow<Theme> = settingsRepository
        .observeSettings()
        .map { settings ->
            Timber.d("Theme updated: ${settings.theme}")
            settings.theme
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Theme.SYSTEM
        )

    /**
     * Dynamic colors preference (Material You).
     * Only applies on Android 12+.
     *
     * Hot Flow: Always has a value (default: true)
     */
    val useDynamicColors: StateFlow<Boolean> = settingsRepository
        .observeSettings()
        .map { settings ->
            Timber.d("Dynamic colors updated: ${settings.useDynamicColors}")
            settings.useDynamicColors
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    /**
     * Update theme preference.
     * Convenience method - delegates to repository.
     *
     * @param theme New theme
     */
    suspend fun setTheme(theme: Theme) {
        Timber.i("Setting theme: $theme")
        settingsRepository.updateTheme(theme)
    }

    /**
     * Update dynamic colors preference.
     * Convenience method - delegates to repository.
     *
     * @param enabled Whether to use dynamic colors
     */
    suspend fun setDynamicColors(enabled: Boolean) {
        Timber.i("Setting dynamic colors: $enabled")
        settingsRepository.updateDynamicColors(enabled)
    }
}