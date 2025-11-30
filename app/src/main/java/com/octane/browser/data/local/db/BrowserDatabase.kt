package com.octane.browser.data.local.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = false
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun tabDao(): TabDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun connectionDao(): ConnectionDao
}


// Add migration in BrowserModule.kt
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new columns with default values
        database.execSQL("""
            ALTER TABLE browser_tabs 
            ADD COLUMN scrollX INTEGER NOT NULL DEFAULT 0
        """)
        database.execSQL("""
            ALTER TABLE browser_tabs 
            ADD COLUMN scrollY INTEGER NOT NULL DEFAULT 0
        """)
        database.execSQL("""
            ALTER TABLE browser_tabs 
            ADD COLUMN canGoBack INTEGER NOT NULL DEFAULT 0
        """)
        database.execSQL("""
            ALTER TABLE browser_tabs 
            ADD COLUMN canGoForward INTEGER NOT NULL DEFAULT 0
        """)
        database.execSQL("""
            ALTER TABLE browser_tabs 
            ADD COLUMN progress INTEGER NOT NULL DEFAULT 0
        """)
        database.execSQL("""
            ALTER TABLE browser_tabs 
            ADD COLUMN isLoading INTEGER NOT NULL DEFAULT 0
        """)
        database.execSQL("""
            ALTER TABLE browser_tabs 
            ADD COLUMN isSecure INTEGER NOT NULL DEFAULT 0
        """)
    }
}
