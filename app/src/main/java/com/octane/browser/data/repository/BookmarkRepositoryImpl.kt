package com.octane.browser.data.repository

import com.octane.browser.data.local.db.dao.BookmarkDao
import com.octane.browser.data.mappers.toDomain
import com.octane.browser.data.mappers.toEntity
import com.octane.browser.domain.models.Bookmark
import com.octane.browser.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookmarkRepositoryImpl(
    private val bookmarkDao: BookmarkDao
) : BookmarkRepository {
    
    override fun getAllBookmarks(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarks().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getBookmarksByFolder(folder: String): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksByFolder(folder).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun insertBookmark(bookmark: Bookmark) {
        bookmarkDao.insertBookmark(bookmark.toEntity())
    }
    
    override suspend fun deleteBookmark(bookmarkId: String) {
        bookmarkDao.deleteBookmark(bookmarkId)
    }
    
    override suspend fun isBookmarked(url: String): Boolean {
        return bookmarkDao.isBookmarked(url)
    }
}