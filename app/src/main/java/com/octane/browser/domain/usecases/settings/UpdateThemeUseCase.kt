package com.octane.browser.domain.usecases.settings

import com.octane.browser.domain.models.Theme
import com.octane.browser.domain.repository.SettingsRepository

/**
 * UseCase: Update theme preference.
 * 
 * Business logic:
 * - Validates theme is one of: LIGHT, DARK, SYSTEM
 * - Persists to DataStore
 * - ThemeManager observes change and updates UI reactively
 * 
 * Usage:
 * ```
 * updateThemeUseCase(Theme.DARK)
 * ```
 */
class UpdateThemeUseCase(
    private val settingsRepository: SettingsRepository
) {
    
    suspend operator fun invoke(theme: Theme) {
        settingsRepository.updateTheme(theme)
    }
}