package com.octane.browser.presentation.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.browser.domain.models.BrowserTab
import com.octane.browser.domain.models.WebViewState
import com.octane.browser.domain.repository.QuickAccessRepository
import com.octane.browser.domain.usecases.bookmark.ToggleBookmarkUseCase
import com.octane.browser.domain.usecases.navigation.NavigateToUrlUseCase
import com.octane.browser.domain.usecases.security.ValidateSslUseCase
import com.octane.browser.domain.usecases.tab.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ✅ ENHANCED: Auto-update Quick Access favicons when browsing
 */
class BrowserViewModel(
    private val navigateToUrlUseCase: NavigateToUrlUseCase,
    private val createNewTabUseCase: CreateNewTabUseCase,
    private val switchTabUseCase: SwitchTabUseCase,
    private val getActiveTabUseCase: GetActiveTabUseCase,
    private val updateTabContentUseCase: UpdateTabContentUseCase,
    private val closeTabUseCase: CloseTabUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val validateSslUseCase: ValidateSslUseCase,
    private val tabRepository: com.octane.browser.domain.repository.TabRepository,
    private val quickAccessRepository: QuickAccessRepository // ✅ NEW: For favicon updates
) : ViewModel() {

    // UI STATE
    private val _webViewState = MutableStateFlow(WebViewState())
    val webViewState: StateFlow<WebViewState> = _webViewState.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    private val _showPhishingWarning = MutableStateFlow<String?>(null)
    val showPhishingWarning: StateFlow<String?> = _showPhishingWarning.asStateFlow()

    private val _isDesktopMode = MutableStateFlow(false)
    val isDesktopMode: StateFlow<Boolean> = _isDesktopMode.asStateFlow()

    val tabs: StateFlow<List<BrowserTab>> = tabRepository.getAllTabs()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    private val _barsVisible = MutableStateFlow(true)
    val barsVisible: StateFlow<Boolean> = _barsVisible.asStateFlow()

    private var currentScrollX = 0
    private var currentScrollY = 0

    init {
        viewModelScope.launch {
            val tabsList = tabRepository.getAllTabs().first()
            if (tabsList.isEmpty()) {
                Timber.d("🆕 No tabs - creating first tab")
                createNewTab()
            } else {
                val activeTab = tabsList.find { it.isActive } ?: tabsList.first()
                loadTabState(activeTab)
                Timber.d("📂 Loaded active tab: ${activeTab.title}")
            }
        }
    }

    fun navigateToHomeScreen() {
        _webViewState.value = WebViewState()
    }

    fun navigateToUrlWithNewTab(url: String, forceNewTab: Boolean = false) {
        viewModelScope.launch {
            if (forceNewTab) {
                saveCurrentTabState()
                val newTab = createNewTabUseCase(url = "", makeActive = true)
                _webViewState.value = WebViewState()
                currentScrollX = 0
                currentScrollY = 0

                val result = navigateToUrlUseCase(newTab.id, url, "")
                if (result is NavigateToUrlUseCase.NavigationResult.Success) {
                    _navigationEvent.emit(NavigationEvent.LoadUrl(result.url))
                    Timber.d("🌐 Created new tab and loading: ${result.url}")
                }
            } else {
                navigateToUrl(url)
            }
        }
    }

    fun navigateToUrl(url: String) {
        viewModelScope.launch {
            val currentTab = tabs.value.find { it.isActive }

            if (currentTab == null) {
                val result = navigateToUrlUseCase.invoke("temp", url, "")
                if (result is NavigateToUrlUseCase.NavigationResult.Success) {
                    val newTab = createNewTabUseCase(url = result.url, makeActive = true)
                    _navigationEvent.emit(NavigationEvent.LoadUrl(result.url))
                    Timber.d("🌐 Created new tab and loading: ${result.url}")
                }
            } else {
                saveCurrentTabState()

                val result = navigateToUrlUseCase(currentTab.id, url)
                if (result is NavigateToUrlUseCase.NavigationResult.Success) {
                    _navigationEvent.emit(NavigationEvent.LoadUrl(result.url))
                    Timber.d("🌐 Loading in active tab: ${result.url}")
                }
            }
        }
    }

    fun createNewTab() {
        viewModelScope.launch {
            saveCurrentTabState()
            createNewTabUseCase(url = "", makeActive = true)

            _webViewState.value = WebViewState()
            currentScrollX = 0
            currentScrollY = 0

            Timber.d("➕ Created new empty tab")
        }
    }

    fun loadTab(tabId: String) {
        viewModelScope.launch {
            saveCurrentTabState()

            val tab = tabs.value.find { it.id == tabId }
            if (tab != null) {
                switchTabUseCase(tabId)
                loadTabState(tab)
                Timber.d("🔄 Loaded tab: ${tab.title}")
            }
        }
    }

    fun switchTab(tabId: String) {
        viewModelScope.launch {
            saveCurrentTabState()
            switchTabUseCase(tabId)

            val tab = tabs.value.find { it.id == tabId }
            if (tab != null) {
                loadTabState(tab)
                Timber.d("🔄 Switched to tab: ${tab.title}")
            }
        }
    }

    fun closeTab(tabId: String) {
        viewModelScope.launch {
            closeTabUseCase(tabId)

            val remainingTabs = tabs.value
            if (remainingTabs.isEmpty()) {
                createNewTab()
            } else {
                val nextTab = remainingTabs.firstOrNull { it.isActive }
                    ?: remainingTabs.first()
                loadTabState(nextTab)
            }
        }
    }

    // TAB STATE PERSISTENCE
    private suspend fun saveCurrentTabState() {
        val currentTab = tabs.value.find { it.isActive } ?: return

        val updatedTab = currentTab.copy(
            url = _webViewState.value.url,
            title = _webViewState.value.title,
            scrollX = currentScrollX,
            scrollY = currentScrollY,
            canGoBack = _webViewState.value.canGoBack,
            canGoForward = _webViewState.value.canGoForward,
            progress = _webViewState.value.progress,
            isLoading = _webViewState.value.isLoading,
            isSecure = _webViewState.value.isSecure,
            timestamp = System.currentTimeMillis()
        )

        tabRepository.updateTab(updatedTab)
        Timber.d("💾 Saved tab state: ${updatedTab.title} (scroll: $currentScrollY)")
    }

    private fun loadTabState(tab: BrowserTab) {
        _webViewState.value = WebViewState(
            url = tab.url,
            title = tab.title,
            progress = tab.progress,
            isLoading = tab.isLoading,
            canGoBack = tab.canGoBack,
            canGoForward = tab.canGoForward,
            isSecure = tab.isSecure
        )

        currentScrollX = tab.scrollX
        currentScrollY = tab.scrollY

        if (tab.url.isNotBlank() && tab.url != "about:blank") {
            _navigationEvent.tryEmit(NavigationEvent.LoadUrl(tab.url))
            _navigationEvent.tryEmit(NavigationEvent.RestoreScroll(tab.scrollX, tab.scrollY))
        }

        Timber.d("📂 Loaded tab state: ${tab.title} (scroll: ${tab.scrollY})")
    }

    // WEBVIEW CALLBACKS
    fun onPageStarted(url: String) {
        _webViewState.value = _webViewState.value.copy(
            url = url,
            isLoading = true,
            progress = 0,
            isSecure = validateSslUseCase(url)
        )
    }

    fun onPageFinished(url: String, title: String) {
        viewModelScope.launch {
            _webViewState.value = _webViewState.value.copy(
                url = url,
                title = title,
                isLoading = false,
                progress = 100
            )

            val currentTab = tabs.value.find { it.isActive }
            if (currentTab != null) {
                updateTabContentUseCase(
                    tabId = currentTab.id,
                    url = url,
                    title = title,
                    favicon = null
                )
            }
        }
    }

    fun onProgressChanged(progress: Int) {
        _webViewState.value = _webViewState.value.copy(progress = progress)
    }

    fun onReceivedTitle(title: String) {
        _webViewState.value = _webViewState.value.copy(title = title)
    }

    fun onReceivedIcon(icon: Bitmap) {
        viewModelScope.launch {
            val currentTab = tabs.value.find { it.isActive } ?: return@launch
            val url = _webViewState.value.url

            // Update tab favicon
            updateTabContentUseCase(
                tabId = currentTab.id,
                url = url,
                title = _webViewState.value.title,
                favicon = icon
            )

            // ✅ ENHANCED: Also update Quick Access favicon if URL matches
            quickAccessRepository.updateFavicon(url, icon)
            Timber.d("🎨 Updated favicon for: $url")
        }
    }

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _webViewState.value = _webViewState.value.copy(
            canGoBack = canGoBack,
            canGoForward = canGoForward
        )
    }

    fun onScrollChanged(scrollX: Int, scrollY: Int) {
        currentScrollX = scrollX
        currentScrollY = scrollY
    }

    fun captureCurrentTabScreenshot(screenshot: Bitmap) {
        viewModelScope.launch {
            val currentTab = tabs.value.find { it.isActive } ?: return@launch
            updateTabContentUseCase(
                tabId = currentTab.id,
                url = _webViewState.value.url,
                title = _webViewState.value.title,
                favicon = null,
                screenshot = screenshot
            )
            Timber.d("📸 Captured screenshot for tab: ${currentTab.id}")
        }
    }

    // NAVIGATION CONTROLS
    fun reload() {
        _navigationEvent.tryEmit(NavigationEvent.Reload)
    }

    fun stopLoading() {
        _navigationEvent.tryEmit(NavigationEvent.StopLoading)
    }

    fun goBack() {
        if (_webViewState.value.canGoBack) {
            _navigationEvent.tryEmit(NavigationEvent.GoBack)
        }
    }

    fun goForward() {
        if (_webViewState.value.canGoForward) {
            _navigationEvent.tryEmit(NavigationEvent.GoForward)
        }
    }

    fun toggleDesktopMode() {
        _isDesktopMode.value = !_isDesktopMode.value
        _navigationEvent.tryEmit(NavigationEvent.SetDesktopMode(_isDesktopMode.value))
        Timber.d("🖥️ Desktop mode: ${_isDesktopMode.value}")
    }

    fun showBars() {
        _barsVisible.value = true
    }

    fun hideBars() {
        _barsVisible.value = false
    }

    // BOOKMARKS
    fun toggleBookmark() {
        viewModelScope.launch {
            val state = _webViewState.value
            toggleBookmarkUseCase(state.url, state.title)
            _isBookmarked.value = !_isBookmarked.value
        }
    }

    // SECURITY
    fun dismissPhishingWarning() {
        _showPhishingWarning.value = null
        goBack()
    }

    fun proceedDespitePhishingWarning() {
        _showPhishingWarning.value = null
    }

    // NAVIGATION EVENTS
    sealed interface NavigationEvent {
        data class LoadUrl(val url: String) : NavigationEvent
        data class RestoreScroll(val x: Int, val y: Int) : NavigationEvent
        data object Reload : NavigationEvent
        data object GoBack : NavigationEvent
        data object GoForward : NavigationEvent
        data object StopLoading : NavigationEvent
        data class ShowError(val message: String) : NavigationEvent
        data class ShowMessage(val message: String) : NavigationEvent
        data class SetDesktopMode(val enabled: Boolean) : NavigationEvent
    }
}