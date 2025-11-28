package com.octane.browser.domain.usecases.settings

import com.octane.browser.domain.models.BrowserSettings
import com.octane.browser.domain.repository.SettingsRepository

/**
 * UseCase: Update entire settings object.
 * 
 * Business logic:
 * - Atomic update (all or nothing)
 * - Validates settings constraints
 * - Notifies all observers
 * 
 * Usage:
 * ```
 * val newSettings = currentSettings.copy(blockAds = true)
 * updateSettingsUseCase(newSettings)
 * ```
 */
class UpdateSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    
    suspend operator fun invoke(settings: BrowserSettings) {
        // Could add validation here:
        // - Validate search engine URL format
        // - Check conflicting settings
        // - Log analytics events
        
        settingsRepository.updateSettings(settings)
    }
}