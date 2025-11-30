package com.octane.browser.presentation.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.browser.domain.models.QuickAccessLink
import com.octane.browser.domain.usecases.AddQuickAccessUseCase
import com.octane.browser.domain.usecases.DeleteQuickAccessUseCase
import com.octane.browser.domain.usecases.GetAllQuickAccessUseCase
import com.octane.browser.domain.usecases.GetQuickAccessCountUseCase
import com.octane.browser.domain.usecases.ReorderQuickAccessUseCase
import com.octane.browser.domain.usecases.UpdateQuickAccessFaviconUseCase
import com.octane.browser.domain.usecases.UpdateQuickAccessUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ✅ ViewModel for Quick Access CRUD operations
 */
class QuickAccessViewModel(
    private val getAllQuickAccessUseCase: GetAllQuickAccessUseCase,
    private val addQuickAccessUseCase: AddQuickAccessUseCase,
    private val updateQuickAccessUseCase: UpdateQuickAccessUseCase,
    private val deleteQuickAccessUseCase: DeleteQuickAccessUseCase,
    private val reorderQuickAccessUseCase: ReorderQuickAccessUseCase,
    private val updateQuickAccessFaviconUseCase: UpdateQuickAccessFaviconUseCase,
    private val getQuickAccessCountUseCase: GetQuickAccessCountUseCase
) : ViewModel() {

    // Quick Access Links
    val quickAccessLinks: StateFlow<List<QuickAccessLink>> = 
        getAllQuickAccessUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // UI State
    private val _uiState = MutableStateFlow<QuickAccessUiState>(QuickAccessUiState.Idle)
    val uiState: StateFlow<QuickAccessUiState> = _uiState.asStateFlow()

    // Add Quick Access
    fun addQuickAccess(url: String, title: String, favicon: Bitmap? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = QuickAccessUiState.Loading
                
                if (url.isBlank() || title.isBlank()) {
                    _uiState.value = QuickAccessUiState.Error("URL and title cannot be empty")
                    return@launch
                }

                val id = addQuickAccessUseCase(url, title, favicon)
                Timber.d("✅ Added Quick Access: $title ($id)")
                _uiState.value = QuickAccessUiState.Success("Quick access added")
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to add quick access")
                _uiState.value = QuickAccessUiState.Error("Failed to add: ${e.message}")
            }
        }
    }

    // Update Quick Access
    fun updateQuickAccess(quickAccess: QuickAccessLink) {
        viewModelScope.launch {
            try {
                updateQuickAccessUseCase(quickAccess)
                Timber.d("✅ Updated Quick Access: ${quickAccess.title}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to update quick access")
                _uiState.value = QuickAccessUiState.Error("Failed to update: ${e.message}")
            }
        }
    }

    // Delete Quick Access
    fun deleteQuickAccess(id: Long) {
        viewModelScope.launch {
            try {
                deleteQuickAccessUseCase(id)
                Timber.d("🗑️ Deleted Quick Access: $id")
                _uiState.value = QuickAccessUiState.Success("Quick access removed")
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete quick access")
                _uiState.value = QuickAccessUiState.Error("Failed to delete: ${e.message}")
            }
        }
    }

    // Reorder Quick Access
    fun reorderQuickAccess(items: List<QuickAccessLink>) {
        viewModelScope.launch {
            try {
                reorderQuickAccessUseCase(items)
                Timber.d("🔄 Reordered Quick Access")
            } catch (e: Exception) {
                Timber.e(e, "Failed to reorder quick access")
            }
        }
    }

    // Update Favicon
    fun updateFavicon(url: String, favicon: Bitmap?) {
        viewModelScope.launch {
            try {
                updateQuickAccessFaviconUseCase(url, favicon)
                Timber.d("🎨 Updated favicon for: $url")
            } catch (e: Exception) {
                Timber.e(e, "Failed to update favicon")
            }
        }
    }

    // Get Count
    suspend fun getCount(): Int {
        return getQuickAccessCountUseCase()
    }

    // Reset UI State
    fun resetUiState() {
        _uiState.value = QuickAccessUiState.Idle
    }

    // Initialize with default links if empty
    fun initializeDefaultLinks() {
        viewModelScope.launch {
            val count = getQuickAccessCountUseCase()
            if (count == 0) {
                Timber.d("📝 Initializing default Quick Access links")
                
                val defaults = listOf(
                    Triple("https://google.com", "Google", null),
                    Triple("https://youtube.com", "YouTube", null),
                    Triple("https://github.com", "GitHub", null),
                    Triple("https://twitter.com", "Twitter", null)
                )
                
                defaults.forEach { (url, title, favicon) ->
                    addQuickAccessUseCase(url, title, favicon)
                }
            }
        }
    }

    sealed class QuickAccessUiState {
        object Idle : QuickAccessUiState()
        object Loading : QuickAccessUiState()
        data class Success(val message: String) : QuickAccessUiState()
        data class Error(val message: String) : QuickAccessUiState()
    }
}