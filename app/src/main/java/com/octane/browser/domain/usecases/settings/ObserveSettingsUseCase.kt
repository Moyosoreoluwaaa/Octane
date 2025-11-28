package com.octane.browser.domain.usecases.settings

import com.octane.browser.domain.models.BrowserSettings
import com.octane.browser.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase: Observe settings reactively.
 * 
 * Business logic:
 * - Returns Flow that emits on every settings change
 * - Initial value emitted immediately
 * - Survives configuration changes
 * 
 * Usage:
 * ```
 * observeSettingsUseCase()
 *     .collect { settings ->
 *         updateUI(settings)
 *     }
 * ```
 */
class ObserveSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    
    operator fun invoke(): Flow<BrowserSettings> {
        return settingsRepository.observeSettings()
    }
}