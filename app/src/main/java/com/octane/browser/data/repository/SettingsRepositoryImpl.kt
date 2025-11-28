package com.octane.browser.data.repository

import com.octane.browser.data.local.datastore.SettingsDataStore
import com.octane.browser.domain.models.BrowserSettings
import com.octane.browser.domain.models.Theme
import com.octane.browser.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Repository implementation for browser settings.
 * 
 * Pattern: Single source of truth - delegates to DataStore.
 * Could be extended to sync with cloud storage, analytics, etc.
 */
class SettingsRepositoryImpl(
    private val dataStore: SettingsDataStore
) : SettingsRepository {
    
    override fun observeSettings(): Flow<BrowserSettings> {
        return dataStore.observeSettings()
    }
    
    override suspend fun updateSettings(settings: BrowserSettings) {
        dataStore.updateSettings(settings)
    }
    
    override suspend fun updateTheme(theme: Theme) {
        dataStore.updateTheme(theme)
    }
    
    override suspend fun updateDynamicColors(enabled: Boolean) {
        dataStore.updateDynamicColors(enabled)
    }
    
    override suspend fun updateJavaScript(enabled: Boolean) {
        dataStore.updateJavaScript(enabled)
    }
    
    override suspend fun updateAdBlocking(enabled: Boolean) {
        dataStore.updateAdBlocking(enabled)
    }
    
    override suspend fun updatePhishingProtection(enabled: Boolean) {
        dataStore.updatePhishingProtection(enabled)
    }
    
    override suspend fun updateWeb3(enabled: Boolean) {
        dataStore.updateWeb3(enabled)
    }
    
    override suspend fun updateSaveHistory(enabled: Boolean) {
        dataStore.updateSaveHistory(enabled)
    }
    
    override suspend fun updateClearDataOnExit(enabled: Boolean) {
        dataStore.updateClearDataOnExit(enabled)
    }
    
    override suspend fun updateSearchEngine(url: String) {
        dataStore.updateSearchEngine(url)
    }
}