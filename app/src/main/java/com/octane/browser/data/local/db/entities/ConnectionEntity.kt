package com.octane.browser.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connections")
data class ConnectionEntity(
    @PrimaryKey val id: String,
    val domain: String,
    val chainId: String,
    val connectedAddress: String,
    val connectedAt: Long,
    val lastUsedAt: Long,
    val permissions: String // Stored as comma-separated
)