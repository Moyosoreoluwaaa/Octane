package com.octane.browser.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabs")
data class TabEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val timestamp: Long,
    val isActive: Boolean
)
