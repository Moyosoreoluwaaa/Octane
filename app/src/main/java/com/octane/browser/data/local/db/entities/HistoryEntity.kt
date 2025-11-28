package com.octane.browser.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "history",
    indices = [Index(value = ["url"])]
)
data class HistoryEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val visitedAt: Long,
    val visitCount: Int
)