package com.octane.browser.domain.usecases.bookmark

import com.octane.browser.domain.models.Bookmark
import com.octane.browser.domain.repository.BookmarkRepository
import java.util.UUID

class AddBookmarkUseCase(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(
        url: String,
        title: String,
        folder: String? = null
    ): Result<Bookmark> {
        if (bookmarkRepository.isBookmarked(url)) {
            return Result.failure(Exception("URL already bookmarked"))
        }

        val bookmark = Bookmark(
            id = UUID.randomUUID().toString(),
            url = url,
            title = title,
            folder = folder,
            createdAt = System.currentTimeMillis()
        )

        bookmarkRepository.insertBookmark(bookmark)
        return Result.success(bookmark)
    }
}