package com.octane.browser.domain.usecases.bookmark

import com.octane.browser.domain.repository.BookmarkRepository

class RemoveBookmarkUseCase(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(bookmarkId: String) {
        bookmarkRepository.deleteBookmark(bookmarkId)
    }
}
