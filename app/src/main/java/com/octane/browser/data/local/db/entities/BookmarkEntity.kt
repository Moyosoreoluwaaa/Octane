package com.octane.browser.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val faviconUrl: String?,
    val folder: String?,
    val createdAt: Long
)