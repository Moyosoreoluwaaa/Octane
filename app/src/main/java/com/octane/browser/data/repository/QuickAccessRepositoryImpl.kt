package com.octane.browser.data.repository

import QuickAccessEntity
import android.graphics.Bitmap
import com.octane.browser.data.local.db.dao.QuickAccessDao
import com.octane.browser.domain.models.QuickAccessLink
import com.octane.browser.domain.repository.QuickAccessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * ✅ Repository Implementation with Entity/Domain mapping
 */
class QuickAccessRepositoryImpl(
    private val quickAccessDao: QuickAccessDao
) : QuickAccessRepository {

    override fun getAllQuickAccess(): Flow<List<QuickAccessLink>> {
        return quickAccessDao.getAllQuickAccess().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getQuickAccessById(id: Long): QuickAccessLink? {
        return quickAccessDao.getQuickAccessById(id)?.toDomain()
    }

    override suspend fun addQuickAccess(url: String, title: String, favicon: Bitmap?): Long {
        val maxPosition = quickAccessDao.getMaxPosition() ?: -1
        val entity = QuickAccessEntity(
            url = url,
            title = title,
            favicon = favicon,
            position = maxPosition + 1,
            createdAt = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis()
        )
        return quickAccessDao.insertQuickAccess(entity)
    }

    override suspend fun updateQuickAccess(quickAccess: QuickAccessLink) {
        quickAccessDao.updateQuickAccess(quickAccess.toEntity())
    }

    override suspend fun deleteQuickAccess(id: Long) {
        quickAccessDao.deleteQuickAccess(id)
    }

    override suspend fun reorderQuickAccess(items: List<QuickAccessLink>) {
        items.forEachIndexed { index, item ->
            quickAccessDao.updatePosition(item.id, index)
        }
    }

    override suspend fun updateFavicon(url: String, favicon: Bitmap?) {
        quickAccessDao.updateFaviconByUrl(url, favicon)
    }

    override suspend fun getCount(): Int {
        return quickAccessDao.getCount()
    }

    // Mappers
    private fun QuickAccessEntity.toDomain() = QuickAccessLink(
        id = id,
        url = url,
        title = title,
        favicon = favicon,
        position = position,
        createdAt = createdAt,
        lastModified = lastModified
    )

    private fun QuickAccessLink.toEntity() = QuickAccessEntity(
        id = id,
        url = url,
        title = title,
        favicon = favicon,
        position = position,
        createdAt = createdAt,
        lastModified = lastModified
    )
}