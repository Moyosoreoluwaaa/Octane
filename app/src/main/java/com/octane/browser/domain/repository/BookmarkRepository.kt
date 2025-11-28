package com.octane.browser.domain.repository

import com.octane.browser.domain.models.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun getAllBookmarks(): Flow<List<Bookmark>>
    fun getBookmarksByFolder(folder: String): Flow<List<Bookmark>>
    suspend fun insertBookmark(bookmark: Bookmark)
    suspend fun deleteBookmark(bookmarkId: String)
    suspend fun isBookmarked(url: String): Boolean
}
