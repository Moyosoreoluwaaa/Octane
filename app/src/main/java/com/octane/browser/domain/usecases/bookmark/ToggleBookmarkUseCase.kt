package com.octane.browser.domain.usecases.bookmark

import com.octane.browser.domain.models.Bookmark
import com.octane.browser.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

class ToggleBookmarkUseCase(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(url: String, title: String): BookmarkAction {
        val allBookmarks = bookmarkRepository.getAllBookmarks().first()
        val existing = allBookmarks.find { it.url == url }

        return if (existing != null) {
            bookmarkRepository.deleteBookmark(existing.id)
            BookmarkAction.Removed
        } else {
            val bookmark = Bookmark(
                id = UUID.randomUUID().toString(),
                url = url,
                title = title,
                createdAt = System.currentTimeMillis()
            )
            bookmarkRepository.insertBookmark(bookmark)
            BookmarkAction.Added
        }
    }

    enum class BookmarkAction {
        Added, Removed
    }
}