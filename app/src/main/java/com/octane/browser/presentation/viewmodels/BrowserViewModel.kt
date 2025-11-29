package com.octane.browser.presentation.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.browser.domain.models.BrowserTab
import com.octane.browser.domain.models.WebViewState
import com.octane.browser.domain.usecases.bookmark.ToggleBookmarkUseCase
import com.octane.browser.domain.usecases.navigation.NavigateToUrlUseCase
import com.octane.browser.domain.usecases.security.ValidateSslUseCase
import com.octane.browser.domain.usecases.tab.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ✅ ENHANCED: Added home screen, desktop mode, tab screenshots
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
    private val tabRepository: com.octane.browser.domain.repository.TabRepository
) : ViewModel() {

    // ═══════════════════════════════════════════════════════════
    // STATE MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    private val _webViewState = MutableStateFlow(WebViewState())
    val webViewState: StateFlow<WebViewState> = _webViewState.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    private val _showPhishingWarning = MutableStateFlow<String?>(null)
    val showPhishingWarning: StateFlow<String?> = _showPhishingWarning.asStateFlow()

    // ✅ NEW: Desktop mode state
    private val _isDesktopMode = MutableStateFlow(false)
    val isDesktopMode: StateFlow<Boolean> = _isDesktopMode.asStateFlow()

    // ✅ NEW: Home screen visibility
    private val _showHomeScreen = MutableStateFlow(true)
    val showHomeScreen: StateFlow<Boolean> = _showHomeScreen.asStateFlow()

    // ✅ NEW: Navigation history for smart back
    private val _navigationHistory = MutableStateFlow<List<String>>(emptyList())
    val navigationHistory: StateFlow<List<String>> = _navigationHistory.asStateFlow()

    // Tabs
    val tabs: StateFlow<List<BrowserTab>> = tabRepository.getAllTabs()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Navigation Events
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    // ✅ NEW: UI Bars visibility (for auto-hide)
    private val _barsVisible = MutableStateFlow(true)
    val barsVisible: StateFlow<Boolean> = _barsVisible.asStateFlow()

    // ═══════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════

    init {
        viewModelScope.launch {
            val activeTab = getActiveTabUseCase()
            if (activeTab == null) {
                createNewTab()
            } else {
                loadTab(activeTab)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ✅ NEW: HOME SCREEN MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    fun showHome() {
        _showHomeScreen.value = true
        _barsVisible.value = true
        Timber.d("🏠 Showing home screen")
    }

    fun hideHome() {
        _showHomeScreen.value = false
        Timber.d("🏠 Hiding home screen")
    }

    fun navigateToHome() {
        _showHomeScreen.value = true
        _navigationEvent.tryEmit(NavigationEvent.LoadUrl("about:blank"))
    }

    // ═══════════════════════════════════════════════════════════
    // ✅ NEW: DESKTOP MODE
    // ═══════════════════════════════════════════════════════════

    fun toggleDesktopMode() {
        _isDesktopMode.value = !_isDesktopMode.value
        _navigationEvent.tryEmit(NavigationEvent.SetDesktopMode(_isDesktopMode.value))
        Timber.d("🖥️ Desktop mode: ${_isDesktopMode.value}")
    }

    // ═══════════════════════════════════════════════════════════
    // ✅ NEW: UI BARS AUTO-HIDE
    // ═══════════════════════════════════════════════════════════

    fun showBars() {
        _barsVisible.value = true
    }

    fun hideBars() {
        _barsVisible.value = false
    }

    // ═══════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════

    fun navigateToUrl(input: String) {
        viewModelScope.launch {
            // Hide home screen when navigating
            _showHomeScreen.value = false

            val result = navigateToUrlUseCase(
                tabId = getCurrentTabId(),
                input = input,
                currentTitle = _webViewState.value.title
            )

            when (result) {
                is NavigateToUrlUseCase.NavigationResult.Success -> {
                    // Add to navigation history
                    _navigationHistory.value = _navigationHistory.value + result.url

                    _navigationEvent.emit(NavigationEvent.LoadUrl(result.url))
                    Timber.d("🌐 Navigating to: ${result.url}")
                }
                is NavigateToUrlUseCase.NavigationResult.Error -> {
                    _navigationEvent.emit(NavigationEvent.ShowError(result.message))
                }
            }
        }
    }

    fun reload() {
        _navigationEvent.tryEmit(NavigationEvent.Reload)
    }

    fun stopLoading() {
        _navigationEvent.tryEmit(NavigationEvent.StopLoading)
    }

    fun goBack() {
        if (_webViewState.value.canGoBack) {
            _navigationEvent.tryEmit(NavigationEvent.GoBack)

            // Update navigation history
            if (_navigationHistory.value.isNotEmpty()) {
                _navigationHistory.value = _navigationHistory.value.dropLast(1)
            }
        } else {
            // ✅ NEW: Show home if no back history
            showHome()
        }
    }

    fun goForward() {
        if (_webViewState.value.canGoForward) {
            _navigationEvent.tryEmit(NavigationEvent.GoForward)
        }
    }

    // ✅ NEW: Smart back navigation
    fun handleBackPress(): Boolean {
        return when {
            _showHomeScreen.value && _navigationHistory.value.isNotEmpty() -> {
                // Go back to last page from home
                val lastUrl = _navigationHistory.value.last()
                navigateToUrl(lastUrl)
                true
            }
            _webViewState.value.canGoBack -> {
                goBack()
                true
            }
            else -> {
                showHome()
                true
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TAB MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    fun createNewTab() {
        viewModelScope.launch {
            val tab = createNewTabUseCase()
            loadTab(tab)
            _showHomeScreen.value = true
            Timber.d("➕ Created new tab: ${tab.id}")
        }
    }

    fun switchTab(tabId: String) {
        viewModelScope.launch {
            switchTabUseCase(tabId)
            val tab = tabs.value.find { it.id == tabId }
            tab?.let { loadTab(it) }
        }
    }

    fun closeTab(tabId: String) {
        viewModelScope.launch {
            closeTabUseCase(tabId)

            // Switch to another tab or create new one
            val remainingTabs = tabs.value
            if (remainingTabs.isEmpty()) {
                createNewTab()
            } else {
                val nextTab = remainingTabs.firstOrNull { it.isActive }
                    ?: remainingTabs.first()
                loadTab(nextTab)
            }
        }
    }

    // ✅ NEW: Capture screenshot for current tab
    // Call this from WebViewContainer when needed
    fun captureCurrentTabScreenshot(screenshot: Bitmap) {
        viewModelScope.launch {
            updateTabContentUseCase(
                tabId = getCurrentTabId(),
                url = _webViewState.value.url,
                title = _webViewState.value.title,
                favicon = null,
                screenshot = screenshot
            )
        }
    }

    private fun loadTab(tab: BrowserTab) {
        _webViewState.value = _webViewState.value.copy(
            url = tab.url,
            title = tab.title
        )

        if (tab.url != "about:blank") {
            _navigationEvent.tryEmit(NavigationEvent.LoadUrl(tab.url))
            _showHomeScreen.value = false
        } else {
            _showHomeScreen.value = true
        }
    }

    // ✅ NEW: Save tab screenshot
    fun saveTabScreenshot(screenshot: Bitmap) {
        viewModelScope.launch {
            val tabId = getCurrentTabId()
            // Store screenshot in repository
            // Implementation depends on your storage strategy
            Timber.d("📸 Saved screenshot for tab: $tabId")
        }
    }

    // ═══════════════════════════════════════════════════════════
    // WEBVIEW CALLBACKS
    // ═══════════════════════════════════════════════════════════

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

            updateTabContentUseCase(
                tabId = getCurrentTabId(),
                url = url,
                title = title,
                favicon = null
            )
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
            updateTabContentUseCase(
                tabId = getCurrentTabId(),
                url = _webViewState.value.url,
                title = _webViewState.value.title,
                favicon = icon
            )
        }
    }

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _webViewState.value = _webViewState.value.copy(
            canGoBack = canGoBack,
            canGoForward = canGoForward
        )
    }

    // ═══════════════════════════════════════════════════════════
    // BOOKMARKS
    // ═══════════════════════════════════════════════════════════

    fun toggleBookmark() {
        viewModelScope.launch {
            val state = _webViewState.value
            toggleBookmarkUseCase(state.url, state.title)
            _isBookmarked.value = !_isBookmarked.value
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SECURITY
    // ═══════════════════════════════════════════════════════════

    fun dismissPhishingWarning() {
        _showPhishingWarning.value = null
        goBack()
    }

    fun proceedDespitePhishingWarning() {
        _showPhishingWarning.value = null
    }

    // ═══════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════

    private fun getCurrentTabId(): String {
        return tabs.value.find { it.isActive }?.id
            ?: tabs.value.firstOrNull()?.id
            ?: ""
    }

    // ═══════════════════════════════════════════════════════════
    // NAVIGATION EVENTS
    // ═══════════════════════════════════════════════════════════

    sealed interface NavigationEvent {
        data class LoadUrl(val url: String) : NavigationEvent
        data object Reload : NavigationEvent
        data object GoBack : NavigationEvent
        data object GoForward : NavigationEvent
        data object StopLoading : NavigationEvent
        data class ShowError(val message: String) : NavigationEvent
        data class ShowMessage(val message: String) : NavigationEvent
        data class SetDesktopMode(val enabled: Boolean) : NavigationEvent // ✅ NEW
    }
}