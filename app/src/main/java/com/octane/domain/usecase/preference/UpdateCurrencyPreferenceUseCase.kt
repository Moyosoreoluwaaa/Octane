package com.octane.domain.usecase.preference

import com.octane.data.local.datastore.UserPreferencesStore
import javax.inject.Inject

/**
 * Updates user currency preference.
 */
class UpdateCurrencyPreferenceUseCase @Inject constructor(
    private val preferencesStore: UserPreferencesStore
) {
    suspend operator fun invoke(currency: String) {
        preferencesStore.setCurrency(currency)
    }
}
