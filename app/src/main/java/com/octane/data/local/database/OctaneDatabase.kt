// app/core/data/local/database/OctaneDatabase.kt

package com.octane.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.octane.data.local.database.converters.OctaneTypeConverters
import com.octane.data.local.database.dao.ApprovalDao
import com.octane.data.local.database.dao.AssetDao
import com.octane.data.local.database.dao.ContactDao
import com.octane.data.local.database.dao.StakingDao
import com.octane.data.local.database.dao.TransactionDao
import com.octane.data.local.database.dao.WalletDao
import com.octane.data.local.database.entities.ApprovalEntity
import com.octane.data.local.database.entities.AssetEntity
import com.octane.data.local.database.entities.ContactEntity
import com.octane.data.local.database.entities.StakingPositionEntity
import com.octane.data.local.database.entities.TransactionEntity
import com.octane.data.local.database.entities.WalletEntity

/**
 * Main Room database for Octane Wallet.
 * Handles all local storage for wallets, transactions, assets, and settings.
 * 
 * Version history:
 * - v1: Initial schema (v0.1-v0.9 features)
 * - v2: Add staking positions (v1.1)
 * - v3: Add multi-chain support (v2.0)
 */
@Database(
    entities = [
        WalletEntity::class,
        TransactionEntity::class,
        AssetEntity::class,
        ContactEntity::class,
        ApprovalEntity::class,
        StakingPositionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(OctaneTypeConverters::class)
abstract class OctaneDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun assetDao(): AssetDao
    abstract fun contactDao(): ContactDao
    abstract fun approvalDao(): ApprovalDao
    abstract fun stakingDao(): StakingDao
    
    companion object {
        const val DATABASE_NAME = "octane_database"
    }
}