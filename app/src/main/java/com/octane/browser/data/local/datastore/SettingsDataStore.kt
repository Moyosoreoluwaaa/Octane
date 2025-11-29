package com.octane.browser.data.local.datastore

import com.octane.browser.domain.models.BrowserSettings
import com.octane.browser.domain.models.Theme
import kotlinx.coroutines.flow.Flow

/**
 * DataStore abstraction for browser settings.
 * Provides reactive access to persisted settings.
 * * Implementation uses DataStore Preferences for type-safe storage.
 */
interface SettingsDataStore {

    /**
     * Observe all browser settings reactively.
     * Emits initial value immediately, then on every change.
     * * @return Flow of current settings
     */
    fun observeSettings(): Flow<BrowserSettings>

    /**
     * Update entire settings object.
     * Persists atomically - either all fields update or none.
     * * @param settings New settings to persist
     */
    suspend fun updateSettings(settings: BrowserSettings)

    /**
     * Update theme preference only.
     * More efficient than updating entire settings object.
     * * @param theme New theme (LIGHT, DARK, SYSTEM)
     */
    suspend fun updateTheme(theme: Theme)

    /**
     * Update dynamic colors preference only.
     * Android 12+ feature - colors from wallpaper.
     * * @param enabled Whether to use dynamic colors
     */
    suspend fun updateDynamicColors(enabled: Boolean)

    /**
     * Update JavaScript setting only.
     * * @param enabled Whether to enable JavaScript
     */
    suspend fun updateJavaScript(enabled: Boolean)

    /**
     * Update ad blocking setting only.
     * * @param enabled Whether to block ads
     */
    suspend fun updateAdBlocking(enabled: Boolean)

    /**
     * Update phishing protection setting only.
     * * @param enabled Whether to enable phishing protection
     */
    suspend fun updatePhishingProtection(enabled: Boolean)

    /**
     * Update Web3 setting only.
     * * @param enabled Whether to enable Web3 features
     */
    suspend fun updateWeb3(enabled: Boolean)

    /**
     * Update history saving setting only.
     * * @param enabled Whether to save browsing history
     */
    suspend fun updateSaveHistory(enabled: Boolean)

    /**
     * Update clear data on exit setting only.
     * * @param enabled Whether to clear data when app closes
     */
    suspend fun updateClearDataOnExit(enabled: Boolean)

    /**
     * Update default search engine.
     * * @param url Search engine URL template (with query placeholder)
     */
    suspend fun updateSearchEngine(url: String)

    /**
     * Observe the preferred tab manager layout type.
     * * @return Flow of Boolean (true for Grid, false for List)
     */
    fun observeTabLayout(): Flow<Boolean>

    /**
     * Persist the user's preferred tab manager layout type.
     * * @param isGrid True if layout should be Grid, false for List
     */
    suspend fun updateTabLayout(isGrid: Boolean)

    /**
     * Clear all settings and reset to defaults.
     * Use cautiously - cannot be undone.
     */
    suspend fun clearAllSettings()
}