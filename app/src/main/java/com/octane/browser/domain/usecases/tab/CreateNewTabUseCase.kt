package com.octane.browser.domain.usecases.tab

import android.graphics.Bitmap
import com.octane.browser.domain.models.BrowserTab
import com.octane.browser.domain.repository.TabRepository
import java.util.UUID

class CreateNewTabUseCase(
    private val tabRepository: TabRepository
) {
    suspend operator fun invoke(
        url: String = "about:blank",
        makeActive: Boolean = true,
        favicon: Bitmap? = null
    ): BrowserTab {
        val newTab = BrowserTab(
            id = UUID.randomUUID().toString(),
            url = url,
            title = "New Tab",
            timestamp = System.currentTimeMillis(),
            isActive = makeActive,
            favicon = favicon
        )

        if (makeActive) {
            tabRepository.setActiveTab(newTab.id)
        }

        tabRepository.insertTab(newTab)
        return newTab
    }
}