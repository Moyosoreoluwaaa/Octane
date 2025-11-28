package com.octane.browser.data.repository

import com.octane.browser.data.local.db.dao.TabDao
import com.octane.browser.data.mappers.toDomain
import com.octane.browser.data.mappers.toEntity
import com.octane.browser.domain.models.BrowserTab
import com.octane.browser.domain.repository.TabRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TabRepositoryImpl(
    private val tabDao: TabDao
) : TabRepository {
    
    override fun getAllTabs(): Flow<List<BrowserTab>> {
        return tabDao.getAllTabs().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getActiveTab(): BrowserTab? {
        return tabDao.getActiveTab()?.toDomain()
    }
    
    override suspend fun insertTab(tab: BrowserTab) {
        tabDao.insertTab(tab.toEntity())
    }
    
    override suspend fun updateTab(tab: BrowserTab) {
        tabDao.updateTab(tab.toEntity())
    }
    
    override suspend fun deleteTab(tabId: String) {
        tabDao.deleteTab(tabId)
    }
    
    override suspend fun setActiveTab(tabId: String) {
        tabDao.deactivateAllTabs()
        tabDao.setActiveTab(tabId)
    }
    
    override suspend fun clearAllTabs() {
        tabDao.clearAllTabs()
    }
}
