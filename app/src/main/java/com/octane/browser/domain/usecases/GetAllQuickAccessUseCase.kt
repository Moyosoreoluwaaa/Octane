package com.octane.browser.domain.usecases

import android.graphics.Bitmap
import com.octane.browser.domain.models.QuickAccessLink
import com.octane.browser.domain.repository.QuickAccessRepository
import kotlinx.coroutines.flow.Flow

/**
 * ✅ Get All Quick Access Links
 */
class GetAllQuickAccessUseCase(
    private val repository: QuickAccessRepository
) {
    operator fun invoke(): Flow<List<QuickAccessLink>> {
        return repository.getAllQuickAccess()
    }
}

/**
 * ✅ Add Quick Access Link
 */
class AddQuickAccessUseCase(
    private val repository: QuickAccessRepository
) {
    suspend operator fun invoke(url: String, title: String, favicon: Bitmap? = null): Long {
        // Validate URL
        val cleanUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        
        return repository.addQuickAccess(cleanUrl, title, favicon)
    }
}

/**
 * ✅ Update Quick Access Link
 */
class UpdateQuickAccessUseCase(
    private val repository: QuickAccessRepository
) {
    suspend operator fun invoke(quickAccess: QuickAccessLink) {
        repository.updateQuickAccess(quickAccess.copy(
            lastModified = System.currentTimeMillis()
        ))
    }
}

/**
 * ✅ Delete Quick Access Link
 */
class DeleteQuickAccessUseCase(
    private val repository: QuickAccessRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteQuickAccess(id)
    }
}

/**
 * ✅ Reorder Quick Access Links
 */
class ReorderQuickAccessUseCase(
    private val repository: QuickAccessRepository
) {
    suspend operator fun invoke(items: List<QuickAccessLink>) {
        repository.reorderQuickAccess(items)
    }
}

/**
 * ✅ Update Favicon for URL
 */
class UpdateQuickAccessFaviconUseCase(
    private val repository: QuickAccessRepository
) {
    suspend operator fun invoke(url: String, favicon: Bitmap?) {
        repository.updateFavicon(url, favicon)
    }
}

/**
 * ✅ Get Quick Access Count
 */
class GetQuickAccessCountUseCase(
    private val repository: QuickAccessRepository
) {
    suspend operator fun invoke(): Int {
        return repository.getCount()
    }
}