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
        ConnectionEntity::class,
        QuickAccessEntity::class // ✅ NEW TABLE
    ],
    version = 3,
    exportSchema = true
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun tabDao(): TabDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun connectionDao(): ConnectionDao
    abstract fun quickAccessDao(): QuickAccessDao // ✅ NEW DAO
}


/**
* ✅ Migration Strategy (Add to your database builder)
*
* Example in your DI setup:
*
* Room.databaseBuilder(context, BrowserDatabase::class.java, DATABASE_NAME)
*     .addMigrations(MIGRATION_1_2)
*     .build()
*/
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create quick_access table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `quick_access` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `url` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `favicon` BLOB,
                `position` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `lastModified` INTEGER NOT NULL
            )
        """)

        // Create index for faster queries
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS `index_quick_access_position` 
            ON `quick_access` (`position`)
        """)
    }
}