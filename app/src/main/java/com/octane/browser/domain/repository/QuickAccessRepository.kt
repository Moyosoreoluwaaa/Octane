package com.octane.browser.domain.repository

import android.graphics.Bitmap
import com.octane.browser.domain.models.QuickAccessLink
import kotlinx.coroutines.flow.Flow

/**
 * ✅ Repository Interface for Quick Access
 */
interface QuickAccessRepository {
    fun getAllQuickAccess(): Flow<List<QuickAccessLink>>
    suspend fun getQuickAccessById(id: Long): QuickAccessLink?
    suspend fun addQuickAccess(url: String, title: String, favicon: Bitmap? = null): Long
    suspend fun updateQuickAccess(quickAccess: QuickAccessLink)
    suspend fun deleteQuickAccess(id: Long)
    suspend fun reorderQuickAccess(items: List<QuickAccessLink>)
    suspend fun updateFavicon(url: String, favicon: Bitmap?)
    suspend fun getCount(): Int
}