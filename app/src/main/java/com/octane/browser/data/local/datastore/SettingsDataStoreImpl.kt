package com.octane.browser.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.octane.browser.domain.models.BrowserSettings
import com.octane.browser.domain.models.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException

/**
 * DataStore implementation for browser settings.
 * Uses DataStore Preferences for type-safe, transactional storage.
 * 
 * Thread-safe: All operations are atomic and conflict-free.
 * Reactive: Changes automatically propagate to all observers.
 */
class SettingsDataStoreImpl(
    private val context: Context
) : SettingsDataStore {
    
    // Lazy DataStore initialization (created on first access)
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "browser_settings"
    )
    
    // Preference Keys (type-safe)
    private object PreferenceKeys {
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val ENABLE_JAVASCRIPT = booleanPreferencesKey("enable_javascript")
        val BLOCK_ADS = booleanPreferencesKey("block_ads")
        val PHISHING_PROTECTION = booleanPreferencesKey("phishing_protection")
        val CLEAR_DATA_ON_EXIT = booleanPreferencesKey("clear_data_on_exit")
        val SAVE_HISTORY = booleanPreferencesKey("save_history")
        val ENABLE_WEB3 = booleanPreferencesKey("enable_web3")
        val THEME = intPreferencesKey("theme")
        val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
    }
    
    override fun observeSettings(): Flow<BrowserSettings> {
        return context.dataStore.data
            .catch { exception ->
                // Handle errors gracefully (corrupted data, IO errors)
                if (exception is IOException) {
                    Timber.e(exception, "Error reading settings, emitting defaults")
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                BrowserSettings(
                    defaultSearchEngine = preferences[PreferenceKeys.SEARCH_ENGINE]
                        ?: "https://www.google.com/search?q=",
                    enableJavaScript = preferences[PreferenceKeys.ENABLE_JAVASCRIPT]
                        ?: true,
                    blockAds = preferences[PreferenceKeys.BLOCK_ADS]
                        ?: false,
                    enablePhishingProtection = preferences[PreferenceKeys.PHISHING_PROTECTION]
                        ?: true,
                    clearDataOnExit = preferences[PreferenceKeys.CLEAR_DATA_ON_EXIT]
                        ?: false,
                    saveHistory = preferences[PreferenceKeys.SAVE_HISTORY]
                        ?: true,
                    enableWeb3 = preferences[PreferenceKeys.ENABLE_WEB3]
                        ?: true,
                    theme = Theme.fromOrdinal(
                        preferences[PreferenceKeys.THEME] ?: Theme.SYSTEM.ordinal
                    ),
                    useDynamicColors = preferences[PreferenceKeys.USE_DYNAMIC_COLORS]
                        ?: true
                )
            }
    }
    
    override suspend fun updateSettings(settings: BrowserSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SEARCH_ENGINE] = settings.defaultSearchEngine
            preferences[PreferenceKeys.ENABLE_JAVASCRIPT] = settings.enableJavaScript
            preferences[PreferenceKeys.BLOCK_ADS] = settings.blockAds
            preferences[PreferenceKeys.PHISHING_PROTECTION] = settings.enablePhishingProtection
            preferences[PreferenceKeys.CLEAR_DATA_ON_EXIT] = settings.clearDataOnExit
            preferences[PreferenceKeys.SAVE_HISTORY] = settings.saveHistory
            preferences[PreferenceKeys.ENABLE_WEB3] = settings.enableWeb3
            preferences[PreferenceKeys.THEME] = settings.theme.ordinal
            preferences[PreferenceKeys.USE_DYNAMIC_COLORS] = settings.useDynamicColors
        }
        Timber.d("Settings updated: $settings")
    }
    
    override suspend fun updateTheme(theme: Theme) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME] = theme.ordinal
        }
        Timber.d("Theme updated: $theme")
    }
    
    override suspend fun updateDynamicColors(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.USE_DYNAMIC_COLORS] = enabled
        }
        Timber.d("Dynamic colors updated: $enabled")
    }
    
    override suspend fun updateJavaScript(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.ENABLE_JAVASCRIPT] = enabled
        }
        Timber.d("JavaScript updated: $enabled")
    }
    
    override suspend fun updateAdBlocking(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.BLOCK_ADS] = enabled
        }
        Timber.d("Ad blocking updated: $enabled")
    }
    
    override suspend fun updatePhishingProtection(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.PHISHING_PROTECTION] = enabled
        }
        Timber.d("Phishing protection updated: $enabled")
    }
    
    override suspend fun updateWeb3(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.ENABLE_WEB3] = enabled
        }
        Timber.d("Web3 updated: $enabled")
    }
    
    override suspend fun updateSaveHistory(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SAVE_HISTORY] = enabled
        }
        Timber.d("Save history updated: $enabled")
    }
    
    override suspend fun updateClearDataOnExit(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.CLEAR_DATA_ON_EXIT] = enabled
        }
        Timber.d("Clear data on exit updated: $enabled")
    }
    
    override suspend fun updateSearchEngine(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SEARCH_ENGINE] = url
        }
        Timber.d("Search engine updated: $url")
    }
    
    override suspend fun clearAllSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        Timber.w("All settings cleared - reset to defaults")
    }
}