package com.octane.domain.usecases.preference

import com.octane.data.local.datastore.UserPreferencesStore

/**
 * Updates user currency preference.
 */
class UpdateCurrencyPreferenceUseCase (
    private val preferencesStore: UserPreferencesStore
) {
    suspend operator fun invoke(currency: String) {
        preferencesStore.setCurrency(currency)
    }
}
