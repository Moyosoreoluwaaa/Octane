package com.octane.browser.domain.repository

import com.octane.browser.domain.models.BrowserTab
import kotlinx.coroutines.flow.Flow

interface TabRepository {
    fun getAllTabs(): Flow<List<BrowserTab>>
    suspend fun getActiveTab(): BrowserTab?
    suspend fun insertTab(tab: BrowserTab)
    suspend fun updateTab(tab: BrowserTab)
    suspend fun deleteTab(tabId: String)
    suspend fun setActiveTab(tabId: String)
    suspend fun clearAllTabs()
}