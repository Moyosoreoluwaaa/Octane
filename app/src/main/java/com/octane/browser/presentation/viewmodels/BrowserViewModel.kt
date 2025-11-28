package com.octane.browser.presentation.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.browser.domain.models.BrowserTab
import com.octane.browser.domain.models.WebViewState
import com.octane.browser.domain.repository.BookmarkRepository
import com.octane.browser.domain.repository.TabRepository
import com.octane.browser.domain.usecases.bookmark.ToggleBookmarkUseCase
import com.octane.browser.domain.usecases.history.RecordVisitUseCase
import com.octane.browser.domain.usecases.navigation.NavigateToUrlUseCase
import com.octane.browser.domain.usecases.security.CheckPhishingUseCase
import com.octane.browser.domain.usecases.security.ValidateSslUseCase
import com.octane.browser.domain.usecases.tab.CloseTabUseCase
import com.octane.browser.domain.usecases.tab.CreateNewTabUseCase
import com.octane.browser.domain.usecases.tab.GetActiveTabUseCase
import com.octane.browser.domain.usecases.tab.SwitchTabUseCase
import com.octane.browser.domain.usecases.tab.UpdateTabContentUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * SHARED ViewModel - Used across all browser screens
 * Manages: Active tab, tabs list, navigation, bookmarks
 */
class BrowserViewModel(
    private val createNewTabUseCase: CreateNewTabUseCase,
    private val closeTabUseCase: CloseTabUseCase,
    private val switchTabUseCase: SwitchTabUseCase,
    private val updateTabContentUseCase: UpdateTabContentUseCase,
    private val getActiveTabUseCase: GetActiveTabUseCase,
    private val navigateToUrlUseCase: NavigateToUrlUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val recordVisitUseCase: RecordVisitUseCase,
    private val checkPhishingUseCase: CheckPhishingUseCase,
    private val validateSslUseCase: ValidateSslUseCase,
    private val bookmarkRepository: BookmarkRepository,
    private val tabRepository: TabRepository
) : ViewModel() {

    // ========================================
    // State Flows (Observed by UI)
    // ========================================

    private val _tabs = MutableStateFlow<List<BrowserTab>>(emptyList())
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTab = MutableStateFlow<BrowserTab?>(null)
    val activeTab: StateFlow<BrowserTab?> = _activeTab.asStateFlow()

    private val _webViewState = MutableStateFlow(WebViewState())
    val webViewState: StateFlow<WebViewState> = _webViewState.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    private val _showPhishingWarning = MutableStateFlow<String?>(null)
    val showPhishingWarning: StateFlow<String?> = _showPhishingWarning.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    // ========================================
    // Initialization
    // ========================================

    init {
        observeTabs()
        loadActiveTab()
    }

    private fun observeTabs() {
        viewModelScope.launch {
            tabRepository.getAllTabs().collect { tabs ->
                _tabs.value = tabs
            }
        }
    }

    private fun loadActiveTab() {
        viewModelScope.launch {
            val active = getActiveTabUseCase()
            if (active == null) {
                // No tabs exist, create first tab
                createNewTab()
            } else {
                _activeTab.value = active
                checkIfBookmarked(active.url)
            }
        }
    }

    // ========================================
    // Tab Management
    // ========================================

    fun createNewTab(url: String = "about:blank") {
        viewModelScope.launch {
            val newTab = createNewTabUseCase(url, makeActive = true)
            _activeTab.value = newTab
            _navigationEvent.emit(NavigationEvent.LoadUrl(url))
        }
    }

    fun switchToTab(tabId: String) {
        viewModelScope.launch {
            switchTabUseCase(tabId)
            val tab = _tabs.value.find { it.id == tabId }
            _activeTab.value = tab
            tab?.let {
                _navigationEvent.emit(NavigationEvent.LoadUrl(it.url))
                checkIfBookmarked(it.url)
            }
        }
    }

    fun closeTab(tabId: String) {
        viewModelScope.launch {
            closeTabUseCase(tabId)
            // Active tab is automatically updated by closeTabUseCase
            loadActiveTab()
        }
    }

    fun closeAllTabs() {
        viewModelScope.launch {
            _tabs.value.forEach { tab ->
                closeTabUseCase(tab.id)
            }
            createNewTab() // Always have at least one tab
        }
    }

    // ========================================
    // Navigation
    // ========================================

    fun navigateToUrl(input: String) {
        val activeTabId = _activeTab.value?.id ?: return

        viewModelScope.launch {
            // Check for phishing
            val securityCheck = checkPhishingUseCase(input)
            if (securityCheck is CheckPhishingUseCase.SecurityCheckResult.Suspicious) {
                _showPhishingWarning.value = securityCheck.reason
                return@launch
            }

            val result = navigateToUrlUseCase(
                tabId = activeTabId,
                input = input,
                currentTitle = _webViewState.value.title
            )

            when (result) {
                is NavigateToUrlUseCase.NavigationResult.Success -> {
                    _navigationEvent.emit(NavigationEvent.LoadUrl(result.url))
                    updateWebViewState(url = result.url, isLoading = true)
                }

                is NavigateToUrlUseCase.NavigationResult.Error -> {
                    _navigationEvent.emit(NavigationEvent.ShowError(result.message))
                }
            }
        }
    }

    fun reload() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.Reload)
        }
    }

    fun goBack() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.GoBack)
        }
    }

    fun goForward() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.GoForward)
        }
    }

    fun stopLoading() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.StopLoading)
            updateWebViewState(isLoading = false)
        }
    }

    // ========================================
    // WebView State Updates (Called from WebViewClient)
    // ========================================

    fun onPageStarted(url: String) {
        updateWebViewState(
            url = url,
            isLoading = true,
            progress = 0,
            isSecure = validateSslUseCase(url)
        )
        checkIfBookmarked(url)
    }

    fun onPageFinished(url: String, title: String) {
        val activeTabId = _activeTab.value?.id ?: return

        viewModelScope.launch {
            updateTabContentUseCase(activeTabId, url, title)
            recordVisitUseCase(url, title)

            updateWebViewState(
                url = url,
                title = title,
                isLoading = false,
                progress = 100
            )
        }
    }

    fun onProgressChanged(progress: Int) {
        updateWebViewState(progress = progress)
    }

    fun onReceivedTitle(title: String) {
        updateWebViewState(title = title)
    }

    fun onReceivedIcon(icon: Bitmap?) {
        // Could store favicon in tab
    }

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        updateWebViewState(canGoBack = canGoBack, canGoForward = canGoForward)
    }

    fun onError(errorMessage: String) {
        updateWebViewState(error = errorMessage, isLoading = false)
    }

    private fun updateWebViewState(
        url: String = _webViewState.value.url,
        title: String = _webViewState.value.title,
        progress: Int = _webViewState.value.progress,
        isLoading: Boolean = _webViewState.value.isLoading,
        canGoBack: Boolean = _webViewState.value.canGoBack,
        canGoForward: Boolean = _webViewState.value.canGoForward,
        isSecure: Boolean = _webViewState.value.isSecure,
        error: String? = _webViewState.value.error
    ) {
        _webViewState.value = WebViewState(
            url = url,
            title = title,
            progress = progress,
            isLoading = isLoading,
            canGoBack = canGoBack,
            canGoForward = canGoForward,
            isSecure = isSecure,
            error = error
        )
    }

    // ========================================
    // Bookmarks
    // ========================================

    fun toggleBookmark() {
        val url = _webViewState.value.url
        val title = _webViewState.value.title

        if (url.isBlank() || url.startsWith("about:")) return

        viewModelScope.launch {
            val action = toggleBookmarkUseCase(url, title)
            _isBookmarked.value = action == ToggleBookmarkUseCase.BookmarkAction.Added

            val message = when (action) {
                ToggleBookmarkUseCase.BookmarkAction.Added -> "Bookmark added"
                ToggleBookmarkUseCase.BookmarkAction.Removed -> "Bookmark removed"
            }
            _navigationEvent.emit(NavigationEvent.ShowMessage(message))
        }
    }

    private fun checkIfBookmarked(url: String) {
        viewModelScope.launch {
            _isBookmarked.value = bookmarkRepository.isBookmarked(url)
        }
    }

    // ========================================
    // Security
    // ========================================

    fun dismissPhishingWarning() {
        _showPhishingWarning.value = null
    }

    fun proceedDespitePhishingWarning() {
        _showPhishingWarning.value = null
        // User acknowledged the risk, proceed with navigation
    }

    // ========================================
    // Events
    // ========================================

    sealed class NavigationEvent {
        data class LoadUrl(val url: String) : NavigationEvent()
        object Reload : NavigationEvent()
        object GoBack : NavigationEvent()
        object GoForward : NavigationEvent()
        object StopLoading : NavigationEvent()
        data class ShowError(val message: String) : NavigationEvent()
        data class ShowMessage(val message: String) : NavigationEvent()
    }
}
