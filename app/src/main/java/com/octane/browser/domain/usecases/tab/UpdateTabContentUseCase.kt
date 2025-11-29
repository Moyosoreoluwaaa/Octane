package com.octane.browser.domain.usecases.tab

import android.graphics.Bitmap
import com.octane.browser.domain.repository.TabRepository
import kotlinx.coroutines.flow.first

/**
 * ✅ ENHANCED: Screenshot parameter added
 */
class UpdateTabContentUseCase(
    private val tabRepository: TabRepository
) {
    suspend operator fun invoke(
        tabId: String,
        url: String,
        title: String,
        favicon: Bitmap?,
        screenshot: Bitmap? = null // ✅ NEW: Optional screenshot
    ) {
        val tab = tabRepository.getAllTabs().first()
            .find { it.id == tabId } ?: return

        val updatedTab = tab.copy(
            url = url,
            title = title,
            timestamp = System.currentTimeMillis(),
            favicon = favicon,
            screenshot = screenshot // ✅ NEW
        )

        tabRepository.updateTab(updatedTab)
    }
}