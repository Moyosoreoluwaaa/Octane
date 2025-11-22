package com.octane.domain.usecases.preference

import com.octane.data.local.datastore.UserPreferencesStore

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