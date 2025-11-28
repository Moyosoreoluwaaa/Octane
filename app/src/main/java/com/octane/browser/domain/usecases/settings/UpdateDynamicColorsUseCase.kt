package com.octane.browser.domain.usecases.settings

import com.octane.browser.domain.repository.SettingsRepository

/**
 * UseCase: Toggle dynamic colors (Material You).
 * 
 * Business logic:
 * - Only available on Android 12+ (checked in UI)
 * - Persists preference
 * - ThemeManager applies wallpaper colors when enabled
 * 
 * Usage:
 * ```
 * updateDynamicColorsUseCase(enabled = true)
 * ```
 */
class UpdateDynamicColorsUseCase(
    private val settingsRepository: SettingsRepository
) {
    
    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.updateDynamicColors(enabled)
    }
}