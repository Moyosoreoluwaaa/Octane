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
import timber.log.Timber

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

    // ✅ NEW: Tab limits based on device capabilities
    companion object {
        private const val MAX_TABS_LOW_END = 5      // <4GB RAM
        private const val MAX_TABS_MID_RANGE = 10   // 4-6GB RAM
        private const val MAX_TABS_HIGH_END = 20    // >6GB RAM

        fun getMaxTabsForDevice(): Int {
            val runtime = Runtime.getRuntime()
            val maxMemoryMB = runtime.maxMemory() / 1024 / 1024

            return when {
                maxMemoryMB < 256 -> MAX_TABS_LOW_END
                maxMemoryMB < 512 -> MAX_TABS_MID_RANGE
                else -> MAX_TABS_HIGH_END
            }.also {
                Timber.d("Device max memory: ${maxMemoryMB}MB, Tab limit: $it")
            }
        }
    }

    private val maxTabs = getMaxTabsForDevice()

    // State Flows
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

    init {
        observeTabs()
        loadActiveTab()
    }

    private fun observeTabs() {
        viewModelScope.launch {
            tabRepository.getAllTabs().collect { tabs ->
                _tabs.value = tabs

                // ✅ Monitor tab count
                if (tabs.size >= maxTabs) {
                    Timber.w("⚠️ Tab limit reached: ${tabs.size}/$maxTabs")
                }
            }
        }
    }

    private fun loadActiveTab() {
        viewModelScope.launch {
            val active = getActiveTabUseCase()
            if (active == null) {
                createNewTab()
            } else {
                _activeTab.value = active
                checkIfBookmarked(active.url)
            }
        }
    }

    // ✅ UPDATED: Tab limit enforcement
    fun createNewTab(url: String = "about:blank") {
        viewModelScope.launch {
            val currentTabs = _tabs.value

            // Check if at limit
            if (currentTabs.size >= maxTabs) {
                Timber.w("Cannot create tab: limit reached ($maxTabs)")

                // Option 1: Close oldest inactive tab
                val oldestInactiveTab = currentTabs
                    .filter { !it.isActive }
                    .minByOrNull { it.timestamp }

                if (oldestInactiveTab != null) {
                    Timber.d("Auto-closing oldest tab: ${oldestInactiveTab.title}")
                    closeTabUseCase(oldestInactiveTab.id)

                    // Show message to user
                    _navigationEvent.emit(
                        NavigationEvent.ShowMessage(
                            "Tab limit reached. Closed oldest tab."
                        )
                    )
                } else {
                    // All tabs are somehow active? Just show error
                    _navigationEvent.emit(
                        NavigationEvent.ShowError(
                            "Maximum $maxTabs tabs allowed. Close some tabs first."
                        )
                    )
                    return@launch
                }
            }

            // Create new tab
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
            loadActiveTab()
        }
    }

    fun closeAllTabs() {
        viewModelScope.launch {
            _tabs.value.forEach { tab ->
                closeTabUseCase(tab.id)
            }
            createNewTab()
        }
    }

    // ✅ NEW: Close inactive tabs to free memory
    fun closeInactiveTabs() {
        viewModelScope.launch {
            val inactiveTabs = _tabs.value.filter { !it.isActive }

            if (inactiveTabs.isEmpty()) {
                _navigationEvent.emit(
                    NavigationEvent.ShowMessage("No inactive tabs to close")
                )
                return@launch
            }

            inactiveTabs.forEach { tab ->
                closeTabUseCase(tab.id)
            }

            _navigationEvent.emit(
                NavigationEvent.ShowMessage("Closed ${inactiveTabs.size} inactive tabs")
            )

            Timber.d("Closed ${inactiveTabs.size} inactive tabs to free memory")
        }
    }

    fun navigateToUrl(input: String) {
        val activeTabId = _activeTab.value?.id ?: return

        viewModelScope.launch {
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
            updateTabContentUseCase(activeTabId, url, title, favicon = null)
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
        val activeTab = _activeTab.value ?: return

        // Check if the icon is different from the currently stored one (optional optimization)
        // if (icon == activeTab.favicon) return

        Timber.d("Received favicon for tab ID: ${activeTab.id}")

        // 🚀 NEW: Update the active tab's favicon in the repository
        viewModelScope.launch {
            // Use the same use case but only pass the favicon, keeping URL/Title intact
            updateTabContentUseCase(
                tabId = activeTab.id,
                url = activeTab.url,
                title = activeTab.title,
                favicon = icon
            )

            // Update the ViewModel's active tab StateFlow immediately for UI reflection
            _activeTab.value = activeTab.copy(favicon = icon)
        }
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

    fun dismissPhishingWarning() {
        _showPhishingWarning.value = null
    }

    fun proceedDespitePhishingWarning() {
        _showPhishingWarning.value = null
    }

    sealed class NavigationEvent {
        data class LoadUrl(val url: String) : NavigationEvent()
        data object Reload : NavigationEvent()
        data object GoBack : NavigationEvent()
        data object GoForward : NavigationEvent()
        data object StopLoading : NavigationEvent()
        data class ShowError(val message: String) : NavigationEvent()
        data class ShowMessage(val message: String) : NavigationEvent()
    }
}