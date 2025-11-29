package com.octane.browser.domain.repository

import com.octane.browser.domain.models.BrowserSettings
import com.octane.browser.domain.models.Theme
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for browser settings.
 * Domain layer contract - no implementation details exposed.
 * 
 * Single source of truth for all app settings.
 */
interface SettingsRepository {
    
    /**
     * Observe all settings reactively.
     * 
     * @return Flow of current settings
     */
    fun observeSettings(): Flow<BrowserSettings>
    
    /**
     * Update entire settings object.
     * 
     * @param settings New settings to persist
     */
    suspend fun updateSettings(settings: BrowserSettings)
    
    /**
     * Update theme preference only.
     * 
     * @param theme New theme (LIGHT, DARK, SYSTEM)
     */
    suspend fun updateTheme(theme: Theme)
    
    /**
     * Update dynamic colors preference only.
     * 
     * @param enabled Whether to use dynamic colors
     */
    suspend fun updateDynamicColors(enabled: Boolean)
    
    /**
     * Update JavaScript setting only.
     * 
     * @param enabled Whether to enable JavaScript
     */
    suspend fun updateJavaScript(enabled: Boolean)
    
    /**
     * Update ad blocking setting only.
     * 
     * @param enabled Whether to block ads
     */
    suspend fun updateAdBlocking(enabled: Boolean)
    
    /**
     * Update phishing protection setting only.
     * 
     * @param enabled Whether to enable phishing protection
     */
    suspend fun updatePhishingProtection(enabled: Boolean)
    
    /**
     * Update Web3 setting only.
     * 
     * @param enabled Whether to enable Web3 features
     */
    suspend fun updateWeb3(enabled: Boolean)
    
    /**
     * Update history saving setting only.
     * 
     * @param enabled Whether to save browsing history
     */
    suspend fun updateSaveHistory(enabled: Boolean)
    
    /**
     * Update clear data on exit setting only.
     * 
     * @param enabled Whether to clear data when app closes
     */
    suspend fun updateClearDataOnExit(enabled: Boolean)
    
    /**
     * Update default search engine.
     * 
     * @param url Search engine URL template
     */
    suspend fun updateSearchEngine(url: String)

    fun observeTabLayout(): Flow<Boolean>
    suspend fun updateTabLayout(isGrid: Boolean)
}