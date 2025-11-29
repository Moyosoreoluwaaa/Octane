package com.octane.browser.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.octane.browser.data.local.db.dao.TabDao
import com.octane.browser.data.local.db.dao.*
import com.octane.browser.data.local.db.entities.*
import com.octane.browser.data.local.db.entity.BrowserTabEntity

@Database(
    entities = [
        BrowserTabEntity::class,
        BookmarkEntity::class,
        HistoryEntity::class,
        ConnectionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun tabDao(): TabDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun connectionDao(): ConnectionDao
}