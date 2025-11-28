package com.octane.browser.domain.models

data class BrowserSettings(
    val defaultSearchEngine: String = "https://www.google.com/search?q=",
    val enableJavaScript: Boolean = true,
    val blockAds: Boolean = false,
    val enablePhishingProtection: Boolean = true,
    val clearDataOnExit: Boolean = false,
    val saveHistory: Boolean = true,
    val enableWeb3: Boolean = true,
    // NEW: Theme settings
    val theme: Theme = Theme.SYSTEM,
    val useDynamicColors: Boolean = true
)
