package com.octane.browser.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.browser.domain.models.Bookmark
import com.octane.browser.domain.repository.BookmarkRepository
import com.octane.browser.domain.usecases.bookmark.AddBookmarkUseCase
import com.octane.browser.domain.usecases.bookmark.RemoveBookmarkUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Bookmarks screen
 * Separate from BrowserViewModel but shares BookmarkRepository
 */
class BookmarkViewModel(
    private val bookmarkRepository: BookmarkRepository,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val removeBookmarkUseCase: RemoveBookmarkUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val bookmarks: StateFlow<List<Bookmark>> = bookmarkRepository.getAllBookmarks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredBookmarks: StateFlow<List<Bookmark>> = combine(
        bookmarks,
        _searchQuery
    ) { bookmarks, query ->
        if (query.isBlank()) {
            bookmarks
        } else {
            bookmarks.filter { bookmark ->
                bookmark.title.contains(query, ignoreCase = true) ||
                        bookmark.url.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addBookmark(url: String, title: String, folder: String? = null) {
        viewModelScope.launch {
            addBookmarkUseCase(url, title, folder)
        }
    }

    fun removeBookmark(bookmarkId: String) {
        viewModelScope.launch {
            removeBookmarkUseCase(bookmarkId)
        }
    }

    fun getBookmarksByFolder(folder: String): StateFlow<List<Bookmark>> {
        return bookmarkRepository.getBookmarksByFolder(folder)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}
