// app/core/data/local/database/entities/ContactEntity.kt

package com.octane.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for saved contacts (v0.6).
 */
@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["address"], unique = true),
        Index(value = ["name"])
    ]
)
data class ContactEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "address")
    val address: String,
    
    @ColumnInfo(name = "chain_id")
    val chainId: String = "solana",
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "last_used")
    val lastUsed: Long
)

// app/core/data/local/database/entities/ApprovalEntity.kt

/**
 * Room entity for token approvals (v1.8).
 */
@Entity(
    tableName = "approvals",
    indices = [
        Index(value = ["wallet_id"]),
        Index(value = ["is_revoked"])
    ]
)
data class ApprovalEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "wallet_id")
    val walletId: String,
    
    @ColumnInfo(name = "chain_id")
    val chainId: String,
    
    @ColumnInfo(name = "token_mint")
    val tokenMint: String,
    
    @ColumnInfo(name = "token_symbol")
    val tokenSymbol: String,
    
    @ColumnInfo(name = "spender_address")
    val spenderAddress: String,
    
    @ColumnInfo(name = "spender_name")
    val spenderName: String?,
    
    @ColumnInfo(name = "allowance")
    val allowance: String, // "UNLIMITED" or specific amount
    
    @ColumnInfo(name = "is_revoked")
    val isRevoked: Boolean = false,
    
    @ColumnInfo(name = "approved_at")
    val approvedAt: Long,
    
    @ColumnInfo(name = "revoked_at")
    val revokedAt: Long? = null
)

// app/core/data/local/database/entities/StakingPositionEntity.kt

/**
 * Room entity for staking positions (v1.1).
 */
@Entity(
    tableName = "staking_positions",
    indices = [
        Index(value = ["wallet_id"]),
        Index(value = ["is_active"])
    ]
)
data class StakingPositionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "wallet_id")
    val walletId: String,
    
    @ColumnInfo(name = "chain_id")
    val chainId: String,
    
    @ColumnInfo(name = "validator_address")
    val validatorAddress: String,
    
    @ColumnInfo(name = "validator_name")
    val validatorName: String,
    
    @ColumnInfo(name = "amount_staked")
    val amountStaked: String,
    
    @ColumnInfo(name = "rewards_earned")
    val rewardsEarned: String,
    
    @ColumnInfo(name = "apy")
    val apy: Double,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "staked_at")
    val stakedAt: Long,
    
    @ColumnInfo(name = "unstaked_at")
    val unstakedAt: Long? = null
)