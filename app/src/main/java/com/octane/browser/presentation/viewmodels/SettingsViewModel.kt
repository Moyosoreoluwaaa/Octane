package com.octane.browser.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.browser.domain.models.BrowserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Settings screen
 * TODO: Persist settings using DataStore
 */
class SettingsViewModel : ViewModel() {

    private val _settings = MutableStateFlow(BrowserSettings())
    val settings: StateFlow<BrowserSettings> = _settings.asStateFlow()

    fun updateSettings(settings: BrowserSettings) {
        viewModelScope.launch {
            _settings.value = settings
            // TODO: Save to DataStore
        }
    }

    fun toggleJavaScript(enabled: Boolean) {
        updateSettings(_settings.value.copy(enableJavaScript = enabled))
    }

    fun toggleAdBlocking(enabled: Boolean) {
        updateSettings(_settings.value.copy(blockAds = enabled))
    }

    fun togglePhishingProtection(enabled: Boolean) {
        updateSettings(_settings.value.copy(enablePhishingProtection = enabled))
    }

    fun toggleWeb3(enabled: Boolean) {
        updateSettings(_settings.value.copy(enableWeb3 = enabled))
    }

    fun setDefaultSearchEngine(url: String) {
        updateSettings(_settings.value.copy(defaultSearchEngine = url))
    }
}

