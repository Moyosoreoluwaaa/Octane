package com.octane.browser.domain.events

sealed class BrowserEvent {
    data class TabCreated(val tabId: String) : BrowserEvent()
    data class TabClosed(val tabId: String) : BrowserEvent()
    data class TabSwitched(val tabId: String) : BrowserEvent()
    data class NavigationStarted(val url: String) : BrowserEvent()
    data class NavigationCompleted(val url: String) : BrowserEvent()
    data class BookmarkAdded(val url: String) : BrowserEvent()
    data class BookmarkRemoved(val url: String) : BrowserEvent()
    data class DAppConnected(val domain: String) : BrowserEvent()
    data class DAppDisconnected(val domain: String) : BrowserEvent()
}
