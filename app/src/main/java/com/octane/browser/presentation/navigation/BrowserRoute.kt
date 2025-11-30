package com.octane.browser.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data class BrowserRoute(
    val url: String = "",
    val tabId: String? = null,  // ✅ NEW: Identify which tab to load
    val forceNewTab: Boolean = false  // ✅ NEW: Force create new tab
)

@Serializable
object HomeRoute

@Serializable
object TabManagerRoute

@Serializable
object BookmarksRoute

@Serializable
object HistoryRoute

@Serializable
object SettingsRoute

@Serializable
object ConnectionsRoute