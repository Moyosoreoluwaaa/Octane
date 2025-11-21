package com.octane.domain.usecase.preference

import com.octane.data.local.datastore.UserPreferencesStore
import javax.inject.Inject

/**
 * Toggles privacy mode (hides balances).
 */

class TogglePrivacyModeUseCase(
    private val preferencesStore: UserPreferencesStore
) {
    suspend operator fun invoke(enabled: Boolean) {
        preferencesStore.setPrivacyMode(enabled)
    }
}