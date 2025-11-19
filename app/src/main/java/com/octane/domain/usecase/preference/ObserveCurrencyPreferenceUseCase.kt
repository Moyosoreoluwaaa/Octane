package com.octane.domain.usecase.preference

import com.octane.data.local.datastore.UserPreferencesStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes user currency preference (USD, EUR, etc.).
 */
class ObserveCurrencyPreferenceUseCase @Inject constructor(
    private val preferencesStore: UserPreferencesStore
) {
    operator fun invoke(): Flow<String> {
        return preferencesStore.currencyPreference
    }
}
