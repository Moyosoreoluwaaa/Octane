package com.octane.browser.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.browser.domain.models.BrowserSettings
import com.octane.browser.domain.usecases.settings.ObserveSettingsUseCase
import com.octane.browser.domain.usecases.settings.UpdateDynamicColorsUseCase
import com.octane.browser.domain.usecases.settings.UpdateSettingsUseCase
import com.octane.browser.domain.usecases.settings.UpdateThemeUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Settings screen.
 *
 * Pattern: Reactive state from repository via UseCases.
 * - Observes settings from DataStore
 * - Updates settings via UseCases
 * - No local state (single source of truth)
 *
 * Lifecycle: New instance per SettingsScreen
 */
class SettingsViewModel(
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val updateDynamicColorsUseCase: UpdateDynamicColorsUseCase
) : ViewModel() {

    /**
     * Current settings from DataStore.
     *
     * Hot Flow: Always has value (defaults if DataStore empty)
     * Lifecycle: Stops 5s after screen destroyed
     */
    val settings: StateFlow<BrowserSettings> = observeSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BrowserSettings()
        )

    /**
     * Update entire settings object.
     * Use when changing multiple settings at once.
     *
     * @param settings New settings
     */
    fun updateSettings(settings: BrowserSettings) {
        viewModelScope.launch {
            updateSettingsUseCase(settings)
        }
    }

    /**
     * Toggle JavaScript setting.
     *
     * @param enabled Whether to enable JavaScript
     */
    fun toggleJavaScript(enabled: Boolean) {
        updateSettings(settings.value.copy(enableJavaScript = enabled))
    }

    /**
     * Toggle ad blocking setting.
     *
     * @param enabled Whether to block ads
     */
    fun toggleAdBlocking(enabled: Boolean) {
        updateSettings(settings.value.copy(blockAds = enabled))
    }

    /**
     * Toggle phishing protection setting.
     *
     * @param enabled Whether to enable phishing protection
     */
    fun togglePhishingProtection(enabled: Boolean) {
        updateSettings(settings.value.copy(enablePhishingProtection = enabled))
    }

    /**
     * Toggle Web3 features.
     *
     * @param enabled Whether to enable Web3
     */
    fun toggleWeb3(enabled: Boolean) {
        updateSettings(settings.value.copy(enableWeb3 = enabled))
    }

    /**
     * Set default search engine.
     *
     * @param url Search engine URL template
     */
    fun setDefaultSearchEngine(url: String) {
        updateSettings(settings.value.copy(defaultSearchEngine = url))
    }
}