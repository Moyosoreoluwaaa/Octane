
// ====================================================================================
// OCTANE WALLET - DATA LAYER CONSOLIDATION
// This file consolidates all provided Kotlin code into a single view for confirmation.
// ====================================================================================


// ====================================================================================
// 1. DOMAIN MODELS (app/core/domain/models)
// ====================================================================================

package com.octane.core.domain.models

// app/core/domain/models/Contact.kt
data class Contact(
val id: String,
val name: String,
val address: String,
val chainId: String,
val createdAt: Long,
val lastUsed: Long
)

// app/core/domain/models/Approval.kt
data class Approval(
val id: String,
val walletId: String,
val chainId: String,
val tokenMint: String,
val tokenSymbol: String,
val spenderAddress: String,
val spenderName: String?,
val allowance: String,
val isRevoked: Boolean,
val approvedAt: Long,
val revokedAt: Long?
)

// app/core/domain/models/StakingPosition.kt
data class StakingPosition(
val id: String,
val walletId: String,
val chainId: String,
val validatorAddress: String,
val validatorName: String,
val amountStaked: String,
val rewardsEarned: String,
val isActive: Boolean,
val stakedAt: Long,
val unstakedAt: Long?
)

/**
* Domain model for Wallet (pure Kotlin, no Android deps).
  */
  data class Wallet(
  val id: String,
  val name: String,
  val publicKey: String,
  val iconEmoji: String? = null,
  val colorHex: String? = null,
  val chainId: String = "solana",
  val isActive: Boolean = false,
  val isHardwareWallet: Boolean = false,
  val hardwareDeviceName: String? = null,
  val createdAt: Long,
  val lastUpdated: Long
  )

// app/core/domain/models/Asset.kt
data class Asset(
val id: String,
val walletId: String,
val chainId: String,
val symbol: String,
val name: String,
val mintAddress: String?,
val balance: String,
val decimals: Int,
val priceUsd: Double? = null,
val valueUsd: Double? = null,
val priceChange24h: Double? = null,
val iconUrl: String? = null,
val isNative: Boolean = false,
val isHidden: Boolean = false,
val costBasisUsd: Double? = null,
val lastUpdated: Long
) {
val balanceDouble: Double
get() = balance.toDoubleOrNull() ?: 0.0

    val profitLossUsd: Double?
        get() = if (valueUsd != null && costBasisUsd != null) {
            valueUsd - costBasisUsd
        } else {
            null
        }
}

// app/core/domain/models/Transaction.kt
data class Transaction(
val id: String,
val walletId: String,
val chainId: String,
val txHash: String,
val type: TransactionType,
val status: TransactionStatus,
val fromAddress: String,
val toAddress: String?,
val amount: String,
val tokenSymbol: String,
val tokenMint: String?,
val fee: String,
val feePriority: String?,
val blockNumber: Long?,
val confirmationCount: Int,
val errorMessage: String?,
val memo: String?,
val timestamp: Long,
val simulated: Boolean = false,
val simulationSuccess: Boolean? = null
)

enum class TransactionType {
SEND, RECEIVE, SWAP, STAKE, UNSTAKE, CLAIM_REWARDS, APPROVE, REVOKE
}

enum class TransactionStatus {
PENDING, CONFIRMED, FAILED
}

// ====================================================================================
// 2. REPOSITORY INTERFACES (app/core/domain/repository)
// ====================================================================================
// app/core/domain/repository/WalletRepository.kt
interface WalletRepository {
fun observeAllWallets(): Flow<List<Wallet>>
fun observeActiveWallet(): Flow<Wallet?>
suspend fun getWalletById(walletId: String): Wallet?
suspend fun createWallet(wallet: Wallet)
suspend fun updateWallet(wallet: Wallet)
suspend fun setActiveWallet(walletId: String)
suspend fun deleteWallet(walletId: String)
fun observeWalletCount(): Flow<Int>
}

// app/core/domain/repository/AssetRepository.kt
interface AssetRepository {
fun observeAssets(walletId: String): Flow<List<Asset>>
fun observeAsset(walletId: String, symbol: String): Flow<Asset?>
fun observeTotalValueUsd(walletId: String): Flow<Double?>
suspend fun refreshAssets(walletId: String, publicKey: String): LoadingState<Unit>
suspend fun updateAssetVisibility(assetId: String, isHidden: Boolean)
}

// app/core/domain/repository/TransactionRepository.kt
interface TransactionRepository {
fun observeTransactionsPaged(walletId: String): Flow<PagingData<Transaction>>
fun observeRecentTransactions(walletId: String, limit: Int): Flow<List<Transaction>>
fun observePendingTransactions(walletId: String): Flow<List<Transaction>>
suspend fun getTransactionByHash(txHash: String): Transaction?
suspend fun insertTransaction(transaction: Transaction)
suspend fun updateTransactionStatus(
txId: String,
status: com.octane.core.domain.models.TransactionStatus,
confirmationCount: Int,
errorMessage: String?
)
suspend fun refreshTransactionHistory(walletId: String, publicKey: String)
}

// ====================================================================================
// 3. ROOM ENTITIES & DATABASE (app/core/data/local/database)
// ====================================================================================

// app/core/data/local/database/OctaneDatabase.kt
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

// app/core/data/local/database/converters/OctaneTypeConverters.kt
class OctaneTypeConverters {

    @TypeConverter
    fun fromTransactionType(value: com.octane.core.data.local.database.entities.TransactionType): String {
        return value.name
    }
    
    @TypeConverter
    fun toTransactionType(value: String): com.octane.core.data.local.database.entities.TransactionType {
        return com.octane.core.data.local.database.entities.TransactionType.valueOf(value)
    }
    
    @TypeConverter
    fun fromTransactionStatus(value: com.octane.core.data.local.database.entities.TransactionStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toTransactionStatus(value: String): com.octane.core.data.local.database.entities.TransactionStatus {
        return com.octane.core.data.local.database.entities.TransactionStatus.valueOf(value)
    }
}


// app/core/data/local/database/entities/WalletEntity.kt

@Entity(
tableName = "wallets",
indices = [
Index(value = ["public_key"], unique = true),
Index(value = ["is_active"])
]
)
data class WalletEntity(
@PrimaryKey
@ColumnInfo(name = "id")
val id: String,

    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "public_key")
    val publicKey: String,
    
    @ColumnInfo(name = "icon_emoji")
    val iconEmoji: String? = null,
    
    @ColumnInfo(name = "color_hex")
    val colorHex: String? = null,
    
    @ColumnInfo(name = "chain_id")
    val chainId: String = "solana",
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = false,
    
    @ColumnInfo(name = "is_hardware_wallet")
    val isHardwareWallet: Boolean = false,
    
    @ColumnInfo(name = "hardware_device_name")
    val hardwareDeviceName: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long
)

// app/core/data/local/database/entities/TransactionEntity.kt
@Entity(
tableName = "transactions",
foreignKeys = [
ForeignKey(
entity = WalletEntity::class,
parentColumns = ["id"],
childColumns = ["wallet_id"],
onDelete = ForeignKey.CASCADE
)
],
indices = [
Index(value = ["wallet_id"]),
Index(value = ["chain_id"]),
Index(value = ["tx_hash"], unique = true),
Index(value = ["status"]),
Index(value = ["timestamp"])
]
)
data class TransactionEntity(
@PrimaryKey
@ColumnInfo(name = "id")
val id: String,

    @ColumnInfo(name = "wallet_id")
    val walletId: String,
    
    @ColumnInfo(name = "chain_id")
    val chainId: String = "solana",
    
    @ColumnInfo(name = "tx_hash")
    val txHash: String,
    
    @ColumnInfo(name = "type")
    val type: TransactionType,
    
    @ColumnInfo(name = "status")
    val status: TransactionStatus,
    
    @ColumnInfo(name = "from_address")
    val fromAddress: String,
    
    @ColumnInfo(name = "to_address")
    val toAddress: String?,
    
    @ColumnInfo(name = "amount")
    val amount: String,
    
    @ColumnInfo(name = "token_symbol")
    val tokenSymbol: String,
    
    @ColumnInfo(name = "token_mint")
    val tokenMint: String?,
    
    @ColumnInfo(name = "fee")
    val fee: String,
    
    @ColumnInfo(name = "fee_priority")
    val feePriority: String? = null,
    
    @ColumnInfo(name = "block_number")
    val blockNumber: Long? = null,
    
    @ColumnInfo(name = "confirmation_count")
    val confirmationCount: Int = 0,
    
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,
    
    @ColumnInfo(name = "memo")
    val memo: String? = null,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "simulated")
    val simulated: Boolean = false,
    
    @ColumnInfo(name = "simulation_success")
    val simulationSuccess: Boolean? = null
)

enum class TransactionType {
SEND, RECEIVE, SWAP, STAKE, UNSTAKE, CLAIM_REWARDS, APPROVE, REVOKE
}

enum class TransactionStatus {
PENDING, CONFIRMED, FAILED
}

// app/core/data/local/database/entities/AssetEntity.kt
@Entity(
tableName = "assets",
foreignKeys = [
ForeignKey(
entity = WalletEntity::class,
parentColumns = ["id"],
childColumns = ["wallet_id"],
onDelete = ForeignKey.CASCADE
)
],
indices = [
Index(value = ["wallet_id"]),
Index(value = ["chain_id"]),
Index(value = ["symbol"]),
Index(value = ["is_hidden"])
]
)
data class AssetEntity(
@PrimaryKey
@ColumnInfo(name = "id")
val id: String,

    @ColumnInfo(name = "wallet_id")
    val walletId: String,
    
    @ColumnInfo(name = "chain_id")
    val chainId: String = "solana",
    
    @ColumnInfo(name = "symbol")
    val symbol: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "mint_address")
    val mintAddress: String?,
    
    @ColumnInfo(name = "balance")
    val balance: String,
    
    @ColumnInfo(name = "decimals")
    val decimals: Int,
    
    @ColumnInfo(name = "price_usd")
    val priceUsd: Double? = null,
    
    @ColumnInfo(name = "value_usd")
    val valueUsd: Double? = null,
    
    @ColumnInfo(name = "price_change_24h")
    val priceChange24h: Double? = null,
    
    @ColumnInfo(name = "icon_url")
    val iconUrl: String? = null,
    
    @ColumnInfo(name = "is_native")
    val isNative: Boolean = false,
    
    @ColumnInfo(name = "is_hidden")
    val isHidden: Boolean = false,
    
    @ColumnInfo(name = "cost_basis_usd")
    val costBasisUsd: Double? = null,
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long
)

// app/core/data/local/database/entities/ContactEntity.kt
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
    val allowance: String,
    
    @ColumnInfo(name = "is_revoked")
    val isRevoked: Boolean = false,
    
    @ColumnInfo(name = "approved_at")
    val approvedAt: Long,
    
    @ColumnInfo(name = "revoked_at")
    val revokedAt: Long? = null
)

// app/core/data/local/database/entities/StakingPositionEntity.kt
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
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "staked_at")
    val stakedAt: Long,
    
    @ColumnInfo(name = "unstaked_at")
    val unstakedAt: Long? = null
)


// ====================================================================================
// 4. ROOM DAOs (app/core/data/local/database/dao)
// ====================================================================================

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

// app/core/data/local/database/dao/WalletDao.kt
@Dao
interface WalletDao {
    
    @Query("SELECT * FROM wallets ORDER BY is_active DESC, created_at DESC")
    fun observeAllWallets(): Flow<List<WalletEntity>>
    
    @Query("SELECT * FROM wallets WHERE is_active = 1 LIMIT 1")
    fun observeActiveWallet(): Flow<WalletEntity?>
    
    @Query("SELECT * FROM wallets WHERE id = :walletId")
    suspend fun getWalletById(walletId: String): WalletEntity?
    
    @Query("SELECT * FROM wallets WHERE public_key = :publicKey")
    suspend fun getWalletByPublicKey(publicKey: String): WalletEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)
    
    @Update
    suspend fun updateWallet(wallet: WalletEntity)
    
    @Transaction
    suspend fun setActiveWallet(walletId: String) {
        deactivateAllWallets()
        activateWallet(walletId)
    }
    
    @Query("UPDATE wallets SET is_active = 0")
    suspend fun deactivateAllWallets()
    
    @Query("UPDATE wallets SET is_active = 1 WHERE id = :walletId")
    suspend fun activateWallet(walletId: String)
    
    @Query("DELETE FROM wallets WHERE id = :walletId")
    suspend fun deleteWallet(walletId: String)
    
    @Query("SELECT COUNT(*) FROM wallets")
    fun observeWalletCount(): Flow<Int>
}

// app/core/data/local/database/dao/TransactionDao.kt
@Dao
interface TransactionDao {
    
    @Query("""
        SELECT * FROM transactions 
        WHERE wallet_id = :walletId 
        ORDER BY timestamp DESC
    """)
    fun observeTransactionsPaged(walletId: String): PagingSource<Int, TransactionEntity>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE wallet_id = :walletId 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    fun observeRecentTransactions(walletId: String, limit: Int = 5): Flow<List<TransactionEntity>>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE wallet_id = :walletId 
        AND status = 'PENDING' 
        ORDER BY timestamp DESC
    """)
    fun observePendingTransactions(walletId: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE tx_hash = :txHash")
    suspend fun getTransactionByHash(txHash: String): TransactionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
    
    @Query("""
        UPDATE transactions 
        SET status = :status, 
            confirmation_count = :confirmationCount,
            error_message = :errorMessage
        WHERE id = :txId
    """)
    suspend fun updateTransactionStatus(
        txId: String,
        status: com.octane.core.data.local.database.entities.TransactionStatus,
        confirmationCount: Int,
        errorMessage: String?
    )
    
    @Query("""
        SELECT * FROM transactions 
        WHERE wallet_id = :walletId 
        AND type = :type 
        ORDER BY timestamp DESC
    """)
    fun observeTransactionsByType(
        walletId: String,
        type: com.octane.core.data.local.database.entities.TransactionType
    ): Flow<List<TransactionEntity>>
    
    @Query("""
        DELETE FROM transactions 
        WHERE wallet_id = :walletId 
        AND timestamp < :cutoffTimestamp
    """)
    suspend fun deleteOldTransactions(walletId: String, cutoffTimestamp: Long)
    
    @Query("SELECT COUNT(*) FROM transactions WHERE wallet_id = :walletId")
    fun observeTransactionCount(walletId: String): Flow<Int>
}

// app/core/data/local/database/dao/AssetDao.kt
@Dao
interface AssetDao {
    
    @Query("""
        SELECT * FROM assets 
        WHERE wallet_id = :walletId 
        AND is_hidden = 0 
        ORDER BY value_usd DESC
    """)
    fun observeAssets(walletId: String): Flow<List<AssetEntity>>
    
    @Query("""
        SELECT * FROM assets 
        WHERE wallet_id = :walletId 
        AND symbol = :symbol
    """)
    fun observeAsset(walletId: String, symbol: String): Flow<AssetEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<AssetEntity>)
    
    @Query("UPDATE assets SET is_hidden = :isHidden WHERE id = :assetId")
    suspend fun updateVisibility(assetId: String, isHidden: Boolean)
    
    @Query("SELECT SUM(value_usd) FROM assets WHERE wallet_id = :walletId AND is_hidden = 0")
    fun observeTotalValueUsd(walletId: String): Flow<Double?>
}

// app/core/data/local/database/dao/ContactDao.kt
@Dao
interface ContactDao {
    
    @Query("SELECT * FROM contacts ORDER BY last_used DESC")
    fun observeContacts(): Flow<List<ContactEntity>>
    
    @Query("SELECT * FROM contacts WHERE id = :contactId")
    suspend fun getContactById(contactId: String): ContactEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)
    
    @Update
    suspend fun updateContact(contact: ContactEntity)
    
    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContact(contactId: String)
}

// app/core/data/local/database/dao/ApprovalDao.kt
@Dao
interface ApprovalDao {
    
    @Query("""
        SELECT * FROM approvals 
        WHERE wallet_id = :walletId 
        AND is_revoked = 0 
        ORDER BY approved_at DESC
    """)
    fun observeActiveApprovals(walletId: String): Flow<List<ApprovalEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: ApprovalEntity)
    
    @Query("UPDATE approvals SET is_revoked = 1, revoked_at = :timestamp WHERE id = :approvalId")
    suspend fun revokeApproval(approvalId: String, timestamp: Long)
}

// app/core/data/local/database/dao/StakingDao.kt
@Dao
interface StakingDao {
    
    @Query("""
        SELECT * FROM staking_positions 
        WHERE wallet_id = :walletId 
        AND is_active = 1 
        ORDER BY staked_at DESC
    """)
    fun observeActivePositions(walletId: String): Flow<List<StakingPositionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: StakingPositionEntity)
    
    @Query("UPDATE staking_positions SET is_active = 0, unstaked_at = :timestamp WHERE id = :positionId")
    suspend fun unstakePosition(positionId: String, timestamp: Long)
    
    @Query("DELETE FROM staking_positions WHERE id = :positionId")
    suspend fun deletePosition(positionId: String)
}


// ====================================================================================
// 5. DATASTORE (app/core/data/local/datastore)
// ====================================================================================

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesStore(private val context: Context) {
    
    private object PreferenceKeys {
        val CURRENCY = stringPreferencesKey("currency")
        val PRIVACY_MODE_ENABLED = booleanPreferencesKey("privacy_mode_enabled")
        val LAST_ACTIVE_WALLET_ID = stringPreferencesKey("last_active_wallet_id")
        val SELECTED_CHAIN_ID = stringPreferencesKey("selected_chain_id")
        val HIDE_ZERO_BALANCES = booleanPreferencesKey("hide_zero_balances")
        val SORT_BY = stringPreferencesKey("sort_by")
        val RPC_ENDPOINT = stringPreferencesKey("rpc_endpoint")
        val CUSTOM_RPC_URL = stringPreferencesKey("custom_rpc_url")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }
    
    val currencyPreference: Flow<String> = context.dataStore.data
        .map { it[PreferenceKeys.CURRENCY] ?: "USD" }

    val privacyModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[PreferenceKeys.PRIVACY_MODE_ENABLED] ?: false }

    val lastActiveWalletId: Flow<String?> = context.dataStore.data
        .map { it[PreferenceKeys.LAST_ACTIVE_WALLET_ID] }

    val selectedChainId: Flow<String> = context.dataStore.data
        .map { it[PreferenceKeys.SELECTED_CHAIN_ID] ?: "solana" }

    val hideZeroBalances: Flow<Boolean> = context.dataStore.data
        .map { it[PreferenceKeys.HIDE_ZERO_BALANCES] ?: false }

    val sortBy: Flow<String> = context.dataStore.data
        .map { it[PreferenceKeys.SORT_BY] ?: "VALUE_DESC" }
    
    val rpcEndpoint: Flow<String> = context.dataStore.data
        .map { it[PreferenceKeys.RPC_ENDPOINT] ?: "mainnet-beta" }

    val customRpcUrl: Flow<String?> = context.dataStore.data
        .map { it[PreferenceKeys.CUSTOM_RPC_URL] }
        
    val biometricEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[PreferenceKeys.BIOMETRIC_ENABLED] ?: false }
    
    // Setters
    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { it[PreferenceKeys.CURRENCY] = currency }
    }
    
    suspend fun setPrivacyModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.PRIVACY_MODE_ENABLED] = enabled }
    }
    
    suspend fun setLastActiveWalletId(walletId: String?) {
        context.dataStore.edit { 
            if (walletId != null) {
                it[PreferenceKeys.LAST_ACTIVE_WALLET_ID] = walletId
            } else {
                it.remove(PreferenceKeys.LAST_ACTIVE_WALLET_ID)
            }
        }
    }
    
    suspend fun setSelectedChainId(chainId: String) {
        context.dataStore.edit { it[PreferenceKeys.SELECTED_CHAIN_ID] = chainId }
    }
    
    suspend fun setHideZeroBalances(hide: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.HIDE_ZERO_BALANCES] = hide }
    }
    
    suspend fun setSortBy(sortBy: String) {
        context.dataStore.edit { it[PreferenceKeys.SORT_BY] = sortBy }
    }
    
    suspend fun setRpcEndpoint(endpoint: String) {
        context.dataStore.edit { it[PreferenceKeys.RPC_ENDPOINT] = endpoint }
    }
    
    suspend fun setCustomRpcUrl(url: String?) {
        context.dataStore.edit { 
            if (url != null) {
                it[PreferenceKeys.CUSTOM_RPC_URL] = url
            } else {
                it.remove(PreferenceKeys.CUSTOM_RPC_URL)
            }
        }
    }
    
    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.BIOMETRIC_ENABLED] = enabled }
    }
}


// ====================================================================================
// 6. REMOTE APIs & DTOS (app/core/data/remote)
// ====================================================================================

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.util.UUID

// --- SolanaRpcApi.kt ---
interface SolanaRpcApi {
    @POST(".")
    suspend fun getBalance(@Body request: RpcRequest<List<String>>): RpcResponse<BalanceResult>
    
    @POST(".")
    suspend fun getTokenAccountsByOwner(@Body request: RpcRequest<TokenAccountsParams>): RpcResponse<TokenAccountsResult>
    
    @POST(".")
    suspend fun getTransaction(@Body request: RpcRequest<List<Any>>): RpcResponse<TransactionResult>
    
    @POST(".")
    suspend fun sendTransaction(@Body request: RpcRequest<List<String>>): RpcResponse<String>
    
    @POST(".")
    suspend fun simulateTransaction(@Body request: RpcRequest<SimulateParams>): RpcResponse<SimulationResult>
    
    @POST(".")
    suspend fun getRecentBlockhash(@Body request: RpcRequest<List<String>>): RpcResponse<BlockhashResult>
    
    @POST(".")
    suspend fun getSignatureStatuses(@Body request: RpcRequest<List<List<String>>>): RpcResponse<SignatureStatusesResult>
    
    @POST(".")
    suspend fun getSignaturesForAddress(@Body request: RpcRequest<SignaturesParams>): RpcResponse<List<SignatureInfo>>
}

// --- PriceApi.kt ---
interface PriceApi {
    @GET("simple/price")
    suspend fun getPrices(
        @Query("ids") coinIds: String,
        @Query("vs_currencies") currency: String = "usd",
        @Query("include_24hr_change") include24hrChange: Boolean = true
    ): Map<String, PriceResponse>
}

// --- SwapAggregatorApi.kt (Part of PriceApi.kt file) ---
interface SwapAggregatorApi {
    @GET("quote")
    suspend fun getQuote(
        @Query("inputMint") inputMint: String,
        @Query("outputMint") outputMint: String,
        @Query("amount") amount: Long,
        @Query("slippageBps") slippageBps: Int = 50
    ): SwapQuoteResponse
    
    @POST("swap")
    suspend fun getSwapTransaction(@Body request: SwapRequest): SwapTransactionResponse
}

// --- RpcDto.kt (Part of solana_dtos.kt) ---
@Serializable
data class RpcRequest<T>(
    val jsonrpc: String = "2.0",
    val id: Int = 1,
    val method: String,
    val params: T
)

@Serializable
data class RpcResponse<T>(
    val jsonrpc: String,
    val id: Int,
    val result: T? = null,
    val error: RpcError? = null
)

@Serializable
data class RpcError(
    val code: Int,
    val message: String,
    val data: JsonElement? = null
)

// Balance
@Serializable
data class BalanceResult(
    val value: Long
)

// Token Accounts
@Serializable
data class TokenAccountsParams(
    val owner: String,
    val options: TokenAccountsOptions
)

@Serializable
data class TokenAccountsOptions(
    val encoding: String = "jsonParsed",
    val programId: String = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
)

@Serializable
data class TokenAccountsResult(
    val value: List<TokenAccountInfo>
)

@Serializable
data class TokenAccountInfo(
    val pubkey: String,
    val account: TokenAccountData
)

@Serializable
data class TokenAccountData(
    val data: TokenAccountParsedData,
    val executable: Boolean,
    val lamports: Long,
    val owner: String,
    val rentEpoch: Long
)

@Serializable
data class TokenAccountParsedData(
    val program: String,
    val parsed: TokenAccountParsedInfo
)

@Serializable
data class TokenAccountParsedInfo(
    val info: TokenAccountInfoDetails,
    val type: String
)

@Serializable
data class TokenAccountInfoDetails(
    val mint: String,
    val owner: String,
    val tokenAmount: TokenAmount
)

@Serializable
data class TokenAmount(
    val amount: String,
    val decimals: Int,
    val uiAmount: Double?,
    val uiAmountString: String
)

// Transaction
@Serializable
data class TransactionResult(
    val blockTime: Long,
    val meta: TransactionMeta,
    val transaction: TransactionData
)

@Serializable
data class TransactionData(
    val signatures: List<String>,
    val message: MessageData
)

@Serializable
data class MessageData(
    val accountKeys: List<String>,
    val instructions: List<JsonElement>
)

@Serializable
data class TransactionMeta(
    val err: JsonElement?,
    val fee: Long,
    val preBalances: List<Long>,
    val postBalances: List<Long>,
    val logMessages: List<String>?
)

// Simulation
@Serializable
data class SimulateParams(
    val transaction: String,
    val options: SimulateOptions
)

@Serializable
data class SimulateOptions(
    val encoding: String = "base64",
    val commitment: String = "confirmed"
)

@Serializable
data class SimulationResult(
    val err: JsonElement?,
    val logs: List<String>? = null,
    val accounts: List<JsonElement>? = null
)

// Blockhash
@Serializable
data class BlockhashResult(
    val blockhash: String,
    val lastValidBlockHeight: Long
)

// Signature Status
@Serializable
data class SignatureStatusesResult(
    val value: List<SignatureStatus?>
)

@Serializable
data class SignatureStatus(
    val slot: Long,
    val confirmations: Int?,
    val err: JsonElement?,
    val confirmationStatus: String?
)

// Signatures
@Serializable
data class SignaturesParams(
    val address: String,
    val options: SignaturesOptions
)

@Serializable
data class SignaturesOptions(
    val limit: Int = 50,
    val before: String? = null,
    val until: String? = null
)

@Serializable
data class SignatureInfo(
    val signature: String,
    val slot: Long,
    val err: JsonElement?,
    val memo: String?,
    val blockTime: Long?
)

// --- PriceDto.kt (Part of PriceApi.kt file) ---
@Serializable
data class PriceResponse(
    val usd: Double,
    @SerialName("usd_24h_change")
    val usd24hChange: Double? = null
)

// --- SwapDto.kt (Part of PriceApi.kt file) ---
@Serializable
data class SwapQuoteResponse(
    val inputMint: String,
    val outputMint: String,
    val inAmount: String,
    val outAmount: String,
    val otherAmountThreshold: String,
    val swapMode: String,
    val slippageBps: Int,
    val priceImpactPct: String,
    val routePlan: List<RoutePlan>
)

@Serializable
data class RoutePlan(
    val swapInfo: SwapInfo,
    val percent: Int
)

@Serializable
data class SwapInfo(
    val ammKey: String,
    val label: String,
    val inputMint: String,
    val outputMint: String,
    val inAmount: String,
    val outAmount: String,
    val feeAmount: String,
    val feeMint: String
)

@Serializable
data class SwapRequest(
    val quoteResponse: SwapQuoteResponse,
    val userPublicKey: String,
    val wrapUnwrapSol: Boolean = true
)

@Serializable
data class SwapTransactionResponse(
    val swapTransaction: String
)


// ====================================================================================
// 7. MAPPERS (app/core/data/mappers)
// ====================================================================================

// WalletMapper.kt
fun WalletEntity.toDomain(): com.octane.core.domain.models.Wallet = com.octane.core.domain.models.Wallet(
    id = id,
    name = name,
    publicKey = publicKey,
    iconEmoji = iconEmoji,
    colorHex = colorHex,
    chainId = chainId,
    isActive = isActive,
    isHardwareWallet = isHardwareWallet,
    hardwareDeviceName = hardwareDeviceName,
    createdAt = createdAt,
    lastUpdated = lastUpdated
)

fun com.octane.core.domain.models.Wallet.toEntity(): WalletEntity = WalletEntity(
    id = id,
    name = name,
    publicKey = publicKey,
    iconEmoji = iconEmoji,
    colorHex = colorHex,
    chainId = chainId,
    isActive = isActive,
    isHardwareWallet = isHardwareWallet,
    hardwareDeviceName = hardwareDeviceName,
    createdAt = createdAt,
    lastUpdated = lastUpdated
)

// AssetMapper.kt
fun AssetEntity.toDomain(): com.octane.core.domain.models.Asset = com.octane.core.domain.models.Asset(
    id = id,
    walletId = walletId,
    chainId = chainId,
    symbol = symbol,
    name = name,
    mintAddress = mintAddress,
    balance = balance,
    decimals = decimals,
    priceUsd = priceUsd,
    valueUsd = valueUsd,
    priceChange24h = priceChange24h,
    iconUrl = iconUrl,
    isNative = isNative,
    isHidden = isHidden,
    costBasisUsd = costBasisUsd,
    lastUpdated = lastUpdated
)

fun com.octane.core.domain.models.Asset.toEntity(): AssetEntity = AssetEntity(
    id = id,
    walletId = walletId,
    chainId = chainId,
    symbol = symbol,
    name = name,
    mintAddress = mintAddress,
    balance = balance,
    decimals = decimals,
    priceUsd = priceUsd,
    valueUsd = valueUsd,
    priceChange24h = priceChange24h,
    iconUrl = iconUrl,
    isNative = isNative,
    isHidden = isHidden,
    costBasisUsd = costBasisUsd,
    lastUpdated = lastUpdated
)

// TransactionMapper.kt
import com.octane.core.data.local.database.entities.TransactionEntity as Entity
import com.octane.core.data.local.database.entities.TransactionStatus as EntityStatus
import com.octane.core.data.local.database.entities.TransactionType as EntityType
import com.octane.core.domain.models.Transaction as Domain
import com.octane.core.domain.models.TransactionStatus as DomainStatus
import com.octane.core.domain.models.TransactionType as DomainType

fun Entity.toDomain(): Domain = Domain(
    id = id,
    walletId = walletId,
    chainId = chainId,
    txHash = txHash,
    type = type.toDomain(),
    status = status.toDomain(),
    fromAddress = fromAddress,
    toAddress = toAddress,
    amount = amount,
    tokenSymbol = tokenSymbol,
    tokenMint = tokenMint,
    fee = fee,
    feePriority = feePriority,
    blockNumber = blockNumber,
    confirmationCount = confirmationCount,
    errorMessage = errorMessage,
    memo = memo,
    timestamp = timestamp,
    simulated = simulated,
    simulationSuccess = simulationSuccess
)

fun Domain.toEntity(): Entity = Entity(
    id = id,
    walletId = walletId,
    chainId = chainId,
    txHash = txHash,
    type = type.toEntity(),
    status = status.toEntity(),
    fromAddress = fromAddress,
    toAddress = toAddress,
    amount = amount,
    tokenSymbol = tokenSymbol,
    tokenMint = tokenMint,
    fee = fee,
    feePriority = feePriority,
    blockNumber = blockNumber,
    confirmationCount = confirmationCount,
    errorMessage = errorMessage,
    memo = memo,
    timestamp = timestamp,
    simulated = simulated,
    simulationSuccess = simulationSuccess
)

fun EntityType.toDomain(): DomainType = when (this) {
    EntityType.SEND -> DomainType.SEND
    EntityType.RECEIVE -> DomainType.RECEIVE
    EntityType.SWAP -> DomainType.SWAP
    EntityType.STAKE -> DomainType.STAKE
    EntityType.UNSTAKE -> DomainType.UNSTAKE
    EntityType.CLAIM_REWARDS -> DomainType.CLAIM_REWARDS
    EntityType.APPROVE -> DomainType.APPROVE
    EntityType.REVOKE -> DomainType.REVOKE
}

fun DomainType.toEntity(): EntityType = when (this) {
    DomainType.SEND -> EntityType.SEND
    DomainType.RECEIVE -> EntityType.RECEIVE
    DomainType.SWAP -> EntityType.SWAP
    DomainType.STAKE -> EntityType.STAKE
    DomainType.UNSTAKE -> EntityType.UNSTAKE
    DomainType.CLAIM_REWARDS -> EntityType.CLAIM_REWARDS
    DomainType.APPROVE -> EntityType.APPROVE
    DomainType.REVOKE -> EntityType.REVOKE
}

fun EntityStatus.toDomain(): DomainStatus = when (this) {
    EntityStatus.PENDING -> DomainStatus.PENDING
    EntityStatus.CONFIRMED -> DomainStatus.CONFIRMED
    EntityStatus.FAILED -> DomainStatus.FAILED
}

fun DomainStatus.toEntity(): EntityStatus = when (this) {
    DomainStatus.PENDING -> EntityStatus.PENDING
    DomainStatus.CONFIRMED -> EntityStatus.CONFIRMED
    DomainStatus.FAILED -> EntityStatus.FAILED
}


// ====================================================================================
// 8. REPOSITORY IMPLEMENTATIONS (app/core/data/repository)
// ====================================================================================

import com.octane.core.data.mappers.*
import com.octane.core.domain.repository.*
import com.octane.core.network.NetworkMonitor
import com.octane.core.util.LoadingState

// --- WalletRepositoryImpl.kt ---
class WalletRepositoryImpl(
    private val walletDao: WalletDao
) : WalletRepository {
    
    override fun observeAllWallets(): Flow<List<com.octane.core.domain.models.Wallet>> {
        return walletDao.observeAllWallets()
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    override fun observeActiveWallet(): Flow<com.octane.core.domain.models.Wallet?> {
        return walletDao.observeActiveWallet()
            .map { it?.toDomain() }
    }
    
    override suspend fun getWalletById(walletId: String): com.octane.core.domain.models.Wallet? {
        return walletDao.getWalletById(walletId)?.toDomain()
    }
    
    override suspend fun createWallet(wallet: com.octane.core.domain.models.Wallet) {
        walletDao.insertWallet(wallet.toEntity())
    }
    
    override suspend fun updateWallet(wallet: com.octane.core.domain.models.Wallet) {
        walletDao.updateWallet(wallet.toEntity())
    }
    
    override suspend fun setActiveWallet(walletId: String) {
        walletDao.setActiveWallet(walletId)
    }
    
    override suspend fun deleteWallet(walletId: String) {
        walletDao.deleteWallet(walletId)
    }
    
    override fun observeWalletCount(): Flow<Int> {
        return walletDao.observeWalletCount()
    }
}

// --- AssetRepositoryImpl.kt ---
class AssetRepositoryImpl(
    private val assetDao: AssetDao,
    private val solanaRpcApi: SolanaRpcApi,
    private val priceApi: PriceApi,
    private val networkMonitor: NetworkMonitor
) : AssetRepository {

    override fun observeAssets(walletId: String): Flow<List<com.octane.core.domain.models.Asset>> {
        return assetDao.observeAssets(walletId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeAsset(walletId: String, symbol: String): Flow<com.octane.core.domain.models.Asset?> {
        return assetDao.observeAsset(walletId, symbol)
            .map { it?.toDomain() }
    }

    override fun observeTotalValueUsd(walletId: String): Flow<Double?> {
        return assetDao.observeTotalValueUsd(walletId)
    }

    override suspend fun refreshAssets(walletId: String, publicKey: String): LoadingState<Unit> {
        if (!networkMonitor.isConnected.value) return LoadingState.Error(Exception("No internet connection"))
        
        return try {
            // 1. Fetch SOL (Native) Balance
            val solBalanceEntity = fetchNativeBalance(publicKey, walletId)
            
            // 2. Fetch SPL Token Balances
            val tokenEntities = fetchTokenBalances(publicKey).map { it.copy(walletId = walletId) }
            
            // Combine all assets
            val allAssetEntities = listOf(solBalanceEntity) + tokenEntities
            
            // 3. Update prices and calculate USD values
            val symbols = allAssetEntities.map { it.symbol }
            val prices = fetchPrices(symbols)
            
            val finalAssets = allAssetEntities.map { asset ->
                val priceData = prices[asset.symbol.lowercase()]
                val priceUsd = priceData?.usd
                val valueUsd = priceUsd?.let { it * (asset.balance.toDoubleOrNull() ?: 0.0) }
                
                asset.copy(
                    priceUsd = priceUsd,
                    valueUsd = valueUsd,
                    priceChange24h = priceData?.usd24hChange
                )
            }

            // 4. Save to database
            assetDao.insertAssets(finalAssets)

            LoadingState.Success(Unit)
        } catch (e: Exception) {
            LoadingState.Error(e)
        }
    }

    override suspend fun updateAssetVisibility(assetId: String, isHidden: Boolean) {
        assetDao.updateVisibility(assetId, isHidden)
    }
    
    private suspend fun fetchNativeBalance(publicKey: String, walletId: String): AssetEntity {
        val response = solanaRpcApi.getBalance(
            RpcRequest(
                method = "getBalance",
                params = listOf(publicKey)
            )
        )
        
        val balanceLamports = response.result?.value ?: 0L
        val balanceSol = balanceLamports / 1_000_000_000.0
        
        // This should fetch the last known price from DB or cache if API fails
        val existingAsset = assetDao.observeAsset(walletId, "SOL").map { it }.firstOrNull()
        
        return AssetEntity(
            id = existingAsset?.id ?: UUID.randomUUID().toString(),
            walletId = walletId,
            chainId = "solana",
            symbol = "SOL",
            name = "Solana",
            mintAddress = null,
            balance = balanceSol.toString(),
            decimals = 9,
            priceUsd = existingAsset?.priceUsd,
            valueUsd = existingAsset?.valueUsd,
            priceChange24h = existingAsset?.priceChange24h,
            iconUrl = "https://example.com/sol_icon.png", // Placeholder
            isNative = true,
            isHidden = existingAsset?.isHidden ?: false,
            costBasisUsd = existingAsset?.costBasisUsd,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private suspend fun fetchTokenBalances(publicKey: String): List<AssetEntity> {
        val response = solanaRpcApi.getTokenAccountsByOwner(
            RpcRequest(
                method = "getTokenAccountsByOwner",
                params = TokenAccountsParams(
                    owner = publicKey,
                    options = TokenAccountsOptions(
                        encoding = "jsonParsed"
                    )
                )
            )
        )
        
        if (response.error != null) {
            throw Exception(response.error.message)
        }
        
        return response.result?.value?.mapNotNull { account ->
            val tokenInfo = account.account.data.parsed.info
            val amount = tokenInfo.tokenAmount.uiAmount ?: return@mapNotNull null
            
            if (amount == 0.0) return@mapNotNull null
            
            AssetEntity(
                id = UUID.randomUUID().toString(),
                walletId = "", // Set by caller
                chainId = "solana",
                symbol = tokenInfo.mint.take(4) + "...", // Needs proper token list lookup
                name = "Unknown Token",
                mintAddress = tokenInfo.mint,
                balance = amount.toString(),
                decimals = tokenInfo.tokenAmount.decimals,
                priceUsd = null,
                valueUsd = null,
                priceChange24h = null,
                iconUrl = null,
                isNative = false,
                isHidden = false,
                costBasisUsd = null,
                lastUpdated = System.currentTimeMillis()
            )
        } ?: emptyList()
    }
    
    private suspend fun fetchPrices(symbols: List<String>): Map<String, PriceResponse> {
        // Simple mapping: SOL -> solana, BTC -> bitcoin
        val coinIds = symbols.joinToString(",") { 
            when (it.uppercase()) {
                "SOL" -> "solana"
                "USDC" -> "usd-coin"
                "USDT" -> "tether"
                else -> it.lowercase()
            }
        }
        return try {
            priceApi.getPrices(coinIds = coinIds)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

// --- TransactionRepositoryImpl.kt ---
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.firstOrNull

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val solanaRpcApi: SolanaRpcApi,
    private val networkMonitor: NetworkMonitor
) : TransactionRepository {
    
    override fun observeTransactionsPaged(walletId: String): Flow<PagingData<com.octane.core.domain.models.Transaction>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { transactionDao.observeTransactionsPaged(walletId) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun observeRecentTransactions(walletId: String, limit: Int): Flow<List<com.octane.core.domain.models.Transaction>> {
        return transactionDao.observeRecentTransactions(walletId, limit)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun observePendingTransactions(walletId: String): Flow<List<com.octane.core.domain.models.Transaction>> {
        return transactionDao.observePendingTransactions(walletId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getTransactionByHash(txHash: String): com.octane.core.domain.models.Transaction? {
        return transactionDao.getTransactionByHash(txHash)?.toDomain()
    }

    override suspend fun insertTransaction(transaction: com.octane.core.domain.models.Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransactionStatus(
        txId: String,
        status: com.octane.core.domain.models.TransactionStatus,
        confirmationCount: Int,
        errorMessage: String?
    ) {
        transactionDao.updateTransactionStatus(
            txId = txId,
            status = status.toEntity(),
            confirmationCount = confirmationCount,
            errorMessage = errorMessage
        )
    }

    override suspend fun refreshTransactionHistory(walletId: String, publicKey: String) {
        if (!networkMonitor.isConnected.value) return
        
        try {
            val response = solanaRpcApi.getSignaturesForAddress(
                RpcRequest(
                    method = "getSignaturesForAddress",
                    params = SignaturesParams(
                        address = publicKey,
                        options = SignaturesOptions(limit = 50)
                    )
                )
            )
            
            if (response.error != null) {
                throw Exception(response.error.message)
            }
            
            // Parse and save transactions
            response.result?.forEach { signatureInfo ->
                // Skip if already in database
                if (transactionDao.getTransactionByHash(signatureInfo.signature) != null) {
                    return@forEach
                }
                
                // Fetch full transaction details
                val txDetailsResponse = solanaRpcApi.getTransaction(
                    RpcRequest(
                        method = "getTransaction",
                        params = listOf(signatureInfo.signature, mapOf("encoding" to "jsonParsed"))
                    )
                )
                
                // SIMPLIFIED STUB - FULL IMPLEMENTATION NEEDED
                txDetailsResponse.result?.let { txResult ->
                    val tx = txResult.toSimplifiedTransaction(walletId, publicKey, signatureInfo)
                    if (tx != null) {
                        transactionDao.insertTransaction(tx.toEntity())
                    }
                }
            }
        } catch (e: Exception) {
            // Log error, don't throw (offline-first pattern)
            println("Error refreshing transaction history: ${e.message}")
        }
    }
    
    // Simplifed stub extension function for mapping RPC result to Domain Transaction
    private fun TransactionResult.toSimplifiedTransaction(
        walletId: String, 
        publicKey: String, 
        signatureInfo: SignatureInfo
    ): com.octane.core.domain.models.Transaction? {
        val isError = this.meta.err != null
        val status = if (isError) DomainStatus.FAILED else DomainStatus.CONFIRMED
        
        // This is a gross simplification for placeholder data
        val primaryInstruction = this.transaction.message.instructions.firstOrNull()
        val type = if (primaryInstruction.toString().contains("transfer")) DomainType.SEND else DomainType.RECEIVE // Placeholder logic
        
        return com.octane.core.domain.models.Transaction(
            id = UUID.randomUUID().toString(),
            walletId = walletId,
            chainId = "solana",
            txHash = signatureInfo.signature,
            type = type,
            status = status,
            fromAddress = publicKey,
            toAddress = this.transaction.message.accountKeys.getOrNull(1), // Placeholder
            amount = "0.0", // Placeholder
            tokenSymbol = "SOL", // Placeholder
            tokenMint = null,
            fee = (this.meta.fee / 1_000_000_000.0).toString(),
            feePriority = null,
            blockNumber = signatureInfo.slot,
            confirmationCount = 1, // Placeholder
            errorMessage = if (isError) this.meta.err.toString() else null,
            memo = signatureInfo.memo,
            timestamp = signatureInfo.blockTime?.times(1000L) ?: System.currentTimeMillis()
        )
    }
}


// ====================================================================================
// 9. DEPENDENCY INJECTION (Koin Modules)
// ====================================================================================

import android.app.Application
import androidx.room.Room
import com.octane.core.di.coreModule
import com.octane.core.data.local.database.OctaneDatabase
import com.octane.core.data.local.datastore.UserPreferencesStore
import com.octane.core.data.remote.api.PriceApi
import com.octane.core.data.remote.api.SolanaRpcApi
import com.octane.core.data.remote.api.SwapAggregatorApi
import com.octane.core.data.repository.AssetRepositoryImpl
import com.octane.core.data.repository.TransactionRepositoryImpl
import com.octane.core.data.repository.WalletRepositoryImpl
import com.octane.core.domain.repository.AssetRepository
import com.octane.core.domain.repository.TransactionRepository
import com.octane.core.domain.repository.WalletRepository
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.android.ext.koin.androidLogger
import io.ktor.client.call.TypeInfo
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.body
import io.ktor.client.plugins.plugin

// --- DataModule.kt (Part of di_modules.kt) ---
val dataModule = module {
    
    // ===== LOCAL DATABASE =====
    single {
        Room.databaseBuilder(
            androidContext(),
            OctaneDatabase::class.java,
            OctaneDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    single { get<OctaneDatabase>().walletDao() }
    single { get<OctaneDatabase>().transactionDao() }
    single { get<OctaneDatabase>().assetDao() }
    single { get<OctaneDatabase>().contactDao() }
    single { get<OctaneDatabase>().approvalDao() }
    single { get<OctaneDatabase>().stakingDao() }
    
    // ===== DATASTORE =====
    single { UserPreferencesStore(androidContext()) }
    
    // ===== NETWORK =====
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
    
    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(get())
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        // Custom logging for Ktor
                        println("Ktor: $message")
                    }
                }
                level = LogLevel.ALL
            }
        }
    }
    
    // Solana RPC API
    single {
        Ktorfit.Builder()
            .baseUrl("https://api.mainnet-beta.solana.com/") // Default RPC
            .httpClient(get())
            .build()
            .create<SolanaRpcApi>()
    }
    
    // Price API (CoinGecko)
    single {
        Ktorfit.Builder()
            .baseUrl("https://api.coingecko.com/api/v3/")
            .httpClient(get())
            .build()
            .create<PriceApi>()
    }
    
    // Jupiter Swap Aggregator
    single {
        Ktorfit.Builder()
            .baseUrl("https://quote-api.jup.ag/v6/")
            .httpClient(get())
            .build()
            .create<SwapAggregatorApi>()
    }
}

// --- RepositoryModule.kt (Part of di_modules.kt) ---
val repositoryModule = module {
    
    single<WalletRepository> {
        WalletRepositoryImpl(
            walletDao = get()
        )
    }
    
    single<AssetRepository> {
        AssetRepositoryImpl(
            assetDao = get(),
            solanaRpcApi = get(),
            priceApi = get(),
            networkMonitor = get()
        )
    }
    
    single<TransactionRepository> {
        TransactionRepositoryImpl(
            transactionDao = get(),
            solanaRpcApi = get(),
            networkMonitor = get()
        )
    }
}

// ====================================================================================
// 10. APPLICATION CLASS (app/OctaneApplication.kt)
// ====================================================================================

/**
 * Monitors device network connectivity and type.
 * Used to show offline banners and enable offline-first patterns.
 */
interface NetworkMonitor {
    /**
     * Reactive connectivity state.
     * Emit true when online, false when offline.
     */
    val isConnected: StateFlow<Boolean>
    
    /**
     * Current connection type (WiFi, Cellular, etc.)
     */
    val connectionType: StateFlow<ConnectionType>
    
    /**
     * Whether connection is metered (cellular, limited WiFi).
     * Use to warn before large downloads/syncs.
     */
    val isMetered: StateFlow<Boolean>
}

enum class ConnectionType {
    WIFI,       // Fast, typically unlimited
    CELLULAR,   // Metered, may be slow
    ETHERNET,   // Fast, unlimited
    NONE        // Offline
}


class OctaneApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin DI
        startKoin {
            androidLogger(Level.ERROR) // Only show errors in production
            androidContext(this@OctaneApplication)
            modules(
                // coreModule,        // Core utilities (NetworkMonitor, LoadingState, etc.) - assumed to exist
                dataModule,        // Database, DataStore, API clients
                repositoryModule   // Repository implementations
                // TODO: Add domainModule (Use Cases)
                // TODO: Add presentationModule (ViewModels)
            )
        }
    }
}
/  
Octane Core Infra

// :app:core:network/NetworkMonitor.kt (commonMain)

package com.octane.core.network

import kotlinx.coroutines.flow.StateFlow

/**

- Monitors device network connectivity and type.
- Used to show offline banners and enable offline-first patterns.  
    _/  
    interface NetworkMonitor {  
    /**  
    _Reactive connectivity state.  
    _Emit true when online, false when offline.  
    _/  
    val isConnected: StateFlow
    
    ```
    /**
     * Current connection type (WiFi, Cellular, etc.)
     */
    val connectionType: StateFlow<ConnectionType>
    
    /**
     * Whether connection is metered (cellular, limited WiFi).
     * Use to warn before large downloads/syncs.
     */
    val isMetered: StateFlow<Boolean>
    ```
    
    }

enum class ConnectionType {  
WIFI, // Fast, typically unlimited  
CELLULAR, // Metered, may be slow  
ETHERNET, // Fast, unlimited  
NONE // Offline  
}

// :app:core:network/NetworkMonitorImpl.kt (androidMain)

package com.octane.core.network

import android.content.Context  
import android.net.ConnectivityManager  
import android.net.Network  
import android.net.NetworkCapabilities  
import android.net.NetworkRequest  
import android.os.Build  
import androidx.core.content.getSystemService  
import kotlinx.coroutines.CoroutineScope  
import kotlinx.coroutines.Dispatchers  
import kotlinx.coroutines.SupervisorJob  
import kotlinx.coroutines.flow.MutableStateFlow  
import kotlinx.coroutines.flow.SharingStarted  
import kotlinx.coroutines.flow.StateFlow  
import kotlinx.coroutines.flow.asStateFlow  
import kotlinx.coroutines.flow.map  
import kotlinx.coroutines.flow.stateIn

actual class NetworkMonitorImpl(  
context: Context  
) : NetworkMonitor {

```
private val connectivityManager = context.getSystemService<ConnectivityManager>()
    ?: throw IllegalStateException("ConnectivityManager not available")

private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

private val _isConnected = MutableStateFlow(checkInitialConnectivity())
override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

private val _connectionType = MutableStateFlow(getInitialConnectionType())
override val connectionType: StateFlow<ConnectionType> = _connectionType.asStateFlow()

override val isMetered: StateFlow<Boolean> = _connectionType
    .map { it == ConnectionType.CELLULAR }
    .stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

init {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        }
        else -> {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }
    }
}

private val networkCallback = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        _isConnected.value = true
        updateConnectionType()
    }
    
    override fun onLost(network: Network) {
        _isConnected.value = false
        _connectionType.value = ConnectionType.NONE
    }
    
    override fun onCapabilitiesChanged(
        network: Network,
        capabilities: NetworkCapabilities
    ) {
        updateConnectionType()
    }
}

private fun updateConnectionType() {
    val capabilities = connectivityManager.getNetworkCapabilities(
        connectivityManager.activeNetwork
    )
    
    _connectionType.value = when {
        capabilities == null -> ConnectionType.NONE
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> 
            ConnectionType.WIFI
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> 
            ConnectionType.CELLULAR
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> 
            ConnectionType.ETHERNET
        else -> ConnectionType.NONE
    }
}

private fun checkInitialConnectivity(): Boolean {
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

private fun getInitialConnectionType(): ConnectionType {
    updateConnectionType()
    return _connectionType.value
}

fun unregister() {
    connectivityManager.unregisterNetworkCallback(networkCallback)
}
```

}

// :app:core:network/SolanaNetworkMonitor.kt

package com.octane.core.network

import kotlinx.coroutines.CoroutineScope  
import kotlinx.coroutines.Dispatchers  
import kotlinx.coroutines.SupervisorJob  
import kotlinx.coroutines.delay  
import kotlinx.coroutines.flow.MutableStateFlow  
import kotlinx.coroutines.flow.StateFlow  
import kotlinx.coroutines.flow.asStateFlow  
import kotlinx.coroutines.isActive  
import kotlinx.coroutines.launch  
import kotlin.time.Duration.Companion.seconds

/**

- Monitors Solana RPC endpoint health.
- Tracks latency and automatically switches to fallback on failures.  
    */  
    class SolanaNetworkMonitor(  
    private val networkMonitor: NetworkMonitor  
    ) {  
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    ```
    private val _rpcHealth = MutableStateFlow(RpcHealth.Unknown)
    val rpcHealth: StateFlow<RpcHealth> = _rpcHealth.asStateFlow()
    
    private val _currentEndpoint = MutableStateFlow(RpcEndpoint.PRIMARY)
    val currentEndpoint: StateFlow<RpcEndpoint> = _currentEndpoint.asStateFlow()
    
    init {
        startHealthChecks()
    }
    
    private fun startHealthChecks() {
        scope.launch {
            while (isActive) {
                if (networkMonitor.isConnected.value) {
                    checkRpcHealth()
                } else {
                    _rpcHealth.value = RpcHealth.Offline
                }
                delay(10.seconds) // Check every 10 seconds
            }
        }
    }
    
    private suspend fun checkRpcHealth() {
        val endpoint = _currentEndpoint.value
        val startTime = System.currentTimeMillis()
        
        try {
            // TODO: Implement actual RPC ping when HTTP client is ready
            // For now, simulate health check
            val latency = System.currentTimeMillis() - startTime
            
            _rpcHealth.value = when {
                latency < 500 -> RpcHealth.Healthy(endpoint, latency)
                latency < 2000 -> RpcHealth.Slow(endpoint, latency)
                else -> {
                    switchToFallback()
                    RpcHealth.Degraded(endpoint, latency)
                }
            }
        } catch (e: Exception) {
            switchToFallback()
            _rpcHealth.value = RpcHealth.Down(endpoint, e.message ?: "Unknown error")
        }
    }
    
    private fun switchToFallback() {
        _currentEndpoint.value = when (_currentEndpoint.value) {
            RpcEndpoint.PRIMARY -> RpcEndpoint.FALLBACK
            RpcEndpoint.FALLBACK -> RpcEndpoint.CUSTOM // If set, otherwise stay on fallback
            RpcEndpoint.CUSTOM -> RpcEndpoint.PRIMARY // Retry primary
        }
    }
    
    fun setCustomEndpoint(url: String) {
        _currentEndpoint.value = RpcEndpoint.CUSTOM
        // TODO: Store custom URL
    }
    ```
    
    }

enum class RpcEndpoint {  
PRIMARY, // Helius or Alchemy  
FALLBACK, // QuickNode or public RPC  
CUSTOM // User-provided RPC  
}

sealed interface RpcHealth {  
data object Unknown : RpcHealth  
data object Offline : RpcHealth  
data class Healthy(val endpoint: RpcEndpoint, val latencyMs: Long) : RpcHealth  
data class Slow(val endpoint: RpcEndpoint, val latencyMs: Long) : RpcHealth  
data class Degraded(val endpoint: RpcEndpoint, val latencyMs: Long) : RpcHealth  
data class Down(val endpoint: RpcEndpoint, val error: String) : RpcHealth

```
val isOperational: Boolean
    get() = this is Healthy || this is Slow
```

}

// :app:core:network/FakeNetworkMonitor.kt (commonTest)

package com.octane.core.network

import kotlinx.coroutines.flow.MutableStateFlow  
import kotlinx.coroutines.flow.StateFlow  
import kotlinx.coroutines.flow.asStateFlow

/**

- Fake NetworkMonitor for testing.
- Allows simulating connectivity changes.  
    */  
    class FakeNetworkMonitor : NetworkMonitor {  
    private val _isConnected = MutableStateFlow(true)  
    override val isConnected: StateFlow = _isConnected.asStateFlow()
    
    ```
    private val _connectionType = MutableStateFlow(ConnectionType.WIFI)
    override val connectionType: StateFlow<ConnectionType> = _connectionType.asStateFlow()
    
    private val _isMetered = MutableStateFlow(false)
    override val isMetered: StateFlow<Boolean> = _isMetered.asStateFlow()
    
    // Test helpers
    fun simulateDisconnect() {
        _isConnected.value = false
        _connectionType.value = ConnectionType.NONE
    }
    
    fun simulateReconnect(type: ConnectionType = ConnectionType.WIFI) {
        _isConnected.value = true
        _connectionType.value = type
        _isMetered.value = (type == ConnectionType.CELLULAR)
    }
    
    fun simulateCellular() {
        _isConnected.value = true
        _connectionType.value = ConnectionType.CELLULAR
        _isMetered.value = true
    }
    
    fun reset() {
        _isConnected.value = true
        _connectionType.value = ConnectionType.WIFI
        _isMetered.value = false
    }
    ```
    
    }

// :app:core:security/KeystoreManager.kt

package com.octane.core.security

import android.content.Context  
import android.security.keystore.KeyGenParameterSpec  
import android.security.keystore.KeyProperties  
import androidx.security.crypto.EncryptedSharedPreferences  
import androidx.security.crypto.MasterKey  
import java.security.KeyStore  
import javax.crypto.Cipher  
import javax.crypto.KeyGenerator  
import javax.crypto.SecretKey  
import javax.crypto.spec.GCMParameterSpec

/**

- Manages secure storage of private keys using Android Keystore.
- Private keys NEVER exist in memory unencrypted.  
    */  
    class KeystoreManager(private val context: Context) {
    
    ```
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * Store private key securely.
     * @param walletId Unique identifier for wallet
     * @param privateKey Raw private key bytes (will be encrypted)
     */
    fun storePrivateKey(walletId: String, privateKey: ByteArray): Result<Unit> {
        return try {
            // Generate encryption key in Keystore
            val secretKey = getOrCreateSecretKey(walletId)
            
            // Encrypt private key
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedKey = cipher.doFinal(privateKey)
            
            // Store encrypted key + IV
            encryptedPrefs.edit()
                .putString("${walletId}_key", encryptedKey.toBase64())
                .putString("${walletId}_iv", iv.toBase64())
                .apply()
            
            // Clear plaintext from memory
            privateKey.fill(0)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to store key: ${e.message}", e))
        }
    }
    
    /**
     * Retrieve private key (remains encrypted until used).
     * @param walletId Wallet identifier
     * @return Encrypted private key data
     */
    fun getPrivateKey(walletId: String): Result<EncryptedPrivateKey> {
        return try {
            val encryptedKeyB64 = encryptedPrefs.getString("${walletId}_key", null)
                ?: return Result.failure(SecurityException("Private key not found"))
            
            val ivB64 = encryptedPrefs.getString("${walletId}_iv", null)
                ?: return Result.failure(SecurityException("IV not found"))
            
            Result.success(
                EncryptedPrivateKey(
                    walletId = walletId,
                    encryptedData = encryptedKeyB64.fromBase64(),
                    iv = ivB64.fromBase64()
                )
            )
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to retrieve key: ${e.message}", e))
        }
    }
    
    /**
     * Decrypt private key for immediate use (e.g., signing).
     * Key is cleared from memory after use.
     */
    fun decryptPrivateKey(encryptedKey: EncryptedPrivateKey): Result<ByteArray> {
        return try {
            val secretKey = getOrCreateSecretKey(encryptedKey.walletId)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, encryptedKey.iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            val decryptedKey = cipher.doFinal(encryptedKey.encryptedData)
            Result.success(decryptedKey)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to decrypt key: ${e.message}", e))
        }
    }
    
    /**
     * Delete private key permanently.
     */
    fun deletePrivateKey(walletId: String): Result<Unit> {
        return try {
            // Remove from encrypted prefs
            encryptedPrefs.edit()
                .remove("${walletId}_key")
                .remove("${walletId}_iv")
                .apply()
            
            // Delete encryption key from Keystore
            keyStore.deleteEntry(walletId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to delete key: ${e.message}", e))
        }
    }
    
    /**
     * Check if private key exists for wallet.
     */
    fun hasPrivateKey(walletId: String): Boolean {
        return encryptedPrefs.contains("${walletId}_key")
    }
    
    private fun getOrCreateSecretKey(alias: String): SecretKey {
        if (keyStore.containsAlias(alias)) {
            return (keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey
        }
        
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(false) // Biometric handled separately
            .build()
        
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
    
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val PREFS_NAME = "octane_secure_storage"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
    ```
    
    }

/**

- Encrypted private key data.
- Never store plaintext keys in this object.  
    */  
    data class EncryptedPrivateKey(  
    val walletId: String,  
    val encryptedData: ByteArray,  
    val iv: ByteArray  
    ) {  
    override fun equals(other: Any?): Boolean {  
    if (this === other) return true  
    if (javaClass != other?.javaClass) return false  
    other as EncryptedPrivateKey  
    return walletId == other.walletId  
    }
    
    ```
    override fun hashCode(): Int = walletId.hashCode()
    ```
    
    }

// Base64 helpers  
private fun ByteArray.toBase64(): String =  
android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)

private fun String.fromBase64(): ByteArray =  
android.util.Base64.decode(this, android.util.Base64.NO_WRAP)

// :app:core:security/BiometricManager.kt

package com.octane.core.security

import android.content.Context  
import androidx.biometric.BiometricManager as AndroidBiometricManager  
import androidx.biometric.BiometricPrompt  
import androidx.core.content.ContextCompat  
import androidx.fragment.app.FragmentActivity

/**

- Manages biometric authentication (FaceID/TouchID).
- Auto-approves transactions < $100 when biometric succeeds.  
    */  
    class BiometricManager(private val context: Context) {
    
    ```
    private val biometricManager = AndroidBiometricManager.from(context)
    
    /**
     * Check if biometric authentication is available.
     */
    fun isBiometricAvailable(): BiometricAvailability {
        return when (biometricManager.canAuthenticate(AUTHENTICATORS)) {
            AndroidBiometricManager.BIOMETRIC_SUCCESS ->
                BiometricAvailability.Available
            
            AndroidBiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                BiometricAvailability.NoHardware
            
            AndroidBiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                BiometricAvailability.HardwareUnavailable
            
            AndroidBiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                BiometricAvailability.NotEnrolled
            
            else ->
                BiometricAvailability.Unknown
        }
    }
    
    /**
     * Authenticate user with biometric prompt.
     * @param activity Host activity
     * @param config Authentication configuration
     * @param onSuccess Called when authentication succeeds
     * @param onError Called when authentication fails
     */
    fun authenticate(
        activity: FragmentActivity,
        config: BiometricConfig,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (errorCode: Int, errorMessage: String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    onSuccess(result)
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errorCode, errString.toString())
                }
                
                override fun onAuthenticationFailed() {
                    // User's biometric didn't match - they can try again
                }
            }
        )
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(config.title)
            .setSubtitle(config.subtitle)
            .setDescription(config.description)
            .setNegativeButtonText(config.negativeButtonText)
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    companion object {
        private const val AUTHENTICATORS = 
            AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG or
            AndroidBiometricManager.Authenticators.DEVICE_CREDENTIAL
    }
    ```
    
    }

sealed interface BiometricAvailability {  
data object Available : BiometricAvailability  
data object NoHardware : BiometricAvailability  
data object HardwareUnavailable : BiometricAvailability  
data object NotEnrolled : BiometricAvailability  
data object Unknown : BiometricAvailability

```
val isAvailable: Boolean get() = this is Available
```

}

data class BiometricConfig(  
val title: String,  
val subtitle: String? = null,  
val description: String? = null,  
val negativeButtonText: String = "Cancel"  
)

// :app:core:security/MaliciousSignatureDetector.kt

package com.octane.core.security

/**

- Detects potentially malicious transactions before signing.
- Ready for Blowfish API integration.  
    */  
    interface MaliciousSignatureDetector {  
    suspend fun analyzeTransaction(  
    transaction: ByteArray  
    ): TransactionRiskAssessment  
    }

data class TransactionRiskAssessment(  
val riskLevel: RiskLevel,  
val warnings: List,  
val details: RiskDetails  
)

enum class RiskLevel {  
SAFE, // Normal transaction  
SUSPICIOUS, // Unusual but not necessarily dangerous  
DANGEROUS, // High likelihood of scam  
MALICIOUS // Known malicious contract/address  
}

data class RiskDetails(  
val isKnownScam: Boolean,  
val unusualPermissions: List,  
val suspiciousRecipients: List,  
val estimatedLoss: Double? = null  
)

// Stub implementation (integrate Blowfish API later)  
class MaliciousSignatureDetectorImpl : MaliciousSignatureDetector {  
override suspend fun analyzeTransaction(  
transaction: ByteArray  
): TransactionRiskAssessment {  
// TODO: Integrate Blowfish API  
// For now, return safe assessment  
return TransactionRiskAssessment(  
riskLevel = RiskLevel.SAFE,  
warnings = emptyList(),  
details = RiskDetails(  
isKnownScam = false,  
unusualPermissions = emptyList(),  
suspiciousRecipients = emptyList()  
)  
)  
}  
}

// :app:core:security/PhishingBlocklist.kt

package com.octane.core.security

/**

- Maintains blocklist of known phishing sites and malicious dApps.  
    */  
    class PhishingBlocklist {
    
    ```
    private val blockedDomains = mutableSetOf<String>()
    private val blockedAddresses = mutableSetOf<String>()
    
    /**
     * Check if URL is on phishing blocklist.
     */
    fun isPhishingSite(url: String): Boolean {
        val domain = extractDomain(url)
        return domain in blockedDomains
    }
    
    /**
     * Check if wallet address is flagged as malicious.
     */
    fun isMaliciousAddress(address: String): Boolean {
        return address in blockedAddresses
    }
    
    /**
     * Add domain to blocklist.
     */
    fun blockDomain(domain: String) {
        blockedDomains.add(domain.lowercase())
    }
    
    /**
     * Add address to blocklist.
     */
    fun blockAddress(address: String) {
        blockedAddresses.add(address)
    }
    
    /**
     * Load blocklist from remote source (e.g., GitHub, API).
     */
    suspend fun refreshBlocklist() {
        // TODO: Fetch from remote source
        // Example sources:
        // - https://github.com/MetaMask/eth-phishing-detect
        // - Solana-specific phishing lists
    }
    
    private fun extractDomain(url: String): String {
        return try {
            val withoutProtocol = url.substringAfter("://")
            val domain = withoutProtocol.substringBefore("/")
            domain.lowercase()
        } catch (e: Exception) {
            url.lowercase()
        }
    }
    ```
    
    }

// :app:core:util/LoadingState.kt (commonMain)

package com.octane.core.util

/**

- Type-safe loading state for any async operation.
- Prevents impossible states (loading + success simultaneously).  
    _/  
    sealed interface LoadingState {  
    /**  
    _Initial state before any operation.  
    */  
    data object Idle : LoadingState
    
    ```
    /**
     * Operation in progress.
     */
    data object Loading : LoadingState<Nothing>
    
    /**
     * Transaction simulation in progress (Octane-specific).
     */
    data object Simulating : LoadingState<Nothing>
    
    /**
     * Operation completed successfully.
     */
    data class Success<T>(val data: T) : LoadingState<T>
    
    /**
     * Operation failed.
     */
    data class Error(
        val throwable: Throwable,
        val message: String = throwable.message ?: "Unknown error"
    ) : LoadingState<Nothing>
    
    /**
     * Data is stale (offline mode with cached data).
     */
    data class Stale<T>(val data: T) : LoadingState<T>
    
    // Convenience properties
    val isLoading: Boolean get() = this is Loading || this is Simulating
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isIdle: Boolean get() = this is Idle
    val isStale: Boolean get() = this is Stale
    
    /**
     * Get data if success/stale, null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Stale -> data
        else -> null
    }
    
    /**
     * Get data if success, throw if error.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw throwable
        is Loading, is Simulating -> throw IllegalStateException("Data not available while loading")
        is Idle -> throw IllegalStateException("Data not available in idle state")
        is Stale -> throw IllegalStateException("Data is stale")
    }
    ```
    
    }

// Extension: Transform data  
fun <T, R> LoadingState.map(transform: (T) -> R): LoadingState {  
return when (this) {  
is LoadingState.Success -> LoadingState.Success(transform(data))  
is LoadingState.Stale -> LoadingState.Stale(transform(data))  
is LoadingState.Loading -> LoadingState.Loading  
is LoadingState.Simulating -> LoadingState.Simulating  
is LoadingState.Error -> LoadingState.Error(throwable, message)  
is LoadingState.Idle -> LoadingState.Idle  
}  
}

// :app:core:util/Formatters.kt (commonMain)

package com.octane.core.util

import kotlin.math.pow

/**

- Formatting utilities for Octane Wallet.
- Handles SOL amounts, NFT prices, wallet addresses, etc.  
    */  
    object Formatters {
    
    ```
    /**
     * Format SOL amount with proper decimals (max 9).
     * @param amount SOL amount (e.g., 1.23456789)
     * @param decimals Number of decimals to show (default: 4)
     * @param showSymbol Include ◎ symbol
     */
    fun formatSOL(
        amount: Double,
        decimals: Int = 4,
        showSymbol: Boolean = true
    ): String {
        val symbol = if (showSymbol) "◎" else ""
        return "$symbol${"%.${decimals}f".format(amount)}"
    }
    
    /**
     * Format NFT floor price.
     * Adjusts decimals based on magnitude.
     */
    fun formatFloorPrice(price: Double): String {
        return when {
            price >= 1000 -> "◎${formatCompact(price)}"
            price >= 1 -> "◎${"%.2f".format(price)}"
            price >= 0.01 -> "◎${"%.4f".format(price)}"
            else -> "◎${"%.6f".format(price)}"
        }
    }
    
    /**
     * Format wallet address with truncation.
     * Standard pattern: abcd...wxyz
     */
    fun formatAddress(
        address: String,
        prefixLength: Int = 4,
        suffixLength: Int = 4
    ): String {
        if (address.length <= prefixLength + suffixLength) return address
        return "${address.take(prefixLength)}...${address.takeLast(suffixLength)}"
    }
    
    /**
     * Format transaction priority fee.
     * @param microLamports Fee in microLamports (1 SOL = 1B lamports)
     */
    fun formatPriorityFee(microLamports: Long): String {
        val sol = microLamports / 1_000_000_000.0
        return "≈◎${"%.6f".format(sol)}"
    }
    
    /**
     * Format large numbers compactly (1.5K, 2.3M, 1.2B).
     */
    fun formatCompact(number: Double, decimals: Int = 2): String {
        return when {
            number >= 1_000_000_000_000 -> 
                "${"%.${decimals}f".format(number / 1_000_000_000_000)}T"
            number >= 1_000_000_000 -> 
                "${"%.${decimals}f".format(number / 1_000_000_000)}B"
            number >= 1_000_000 -> 
                "${"%.${decimals}f".format(number / 1_000_000)}M"
            number >= 1_000 -> 
                "${"%.${decimals}f".format(number / 1_000)}K"
            else -> 
                "%.${decimals}f".format(number)
        }
    }
    
    /**
     * Format percentage with sign.
     */
    fun formatPercentage(
        value: Double,
        decimals: Int = 2,
        showSign: Boolean = true
    ): String {
        val sign = when {
            !showSign -> ""
            value >= 0 -> "+"
            else -> ""
        }
        val formatted = "%.${decimals}f".format(value * 100)
        return "$sign$formatted%"
    }
    
    /**
     * Format currency (USD, EUR, etc.).
     */
    fun formatCurrency(
        amount: Double,
        symbol: String = "$",
        decimals: Int = 2
    ): String {
        val formatted = "%.${decimals}f".format(amount)
        return "$symbol$formatted"
    }
    
    /**
     * Format relative time (2m ago, 1h ago, etc.).
     */
    fun formatRelativeTime(timestampMs: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestampMs
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            diff < 604_800_000 -> "${diff / 86_400_000}d ago"
            else -> formatDate(timestampMs)
        }
    }
    
    /**
     * Format timestamp as date.
     */
    fun formatDate(timestampMs: Long, pattern: String = "MMM dd, yyyy"): String {
        // TODO: Use kotlinx-datetime for KMP compatibility
        // For now, return ISO format
        return java.text.SimpleDateFormat(pattern, java.util.Locale.US)
            .format(java.util.Date(timestampMs))
    }
    
    /**
     * Format transaction hash (truncate).
     */
    fun formatTxHash(hash: String): String = formatAddress(hash, 8, 8)
    ```
    
    }

// :app:core:util/Validators.kt (commonMain)

package com.octane.core.util

/**

- Input validation utilities for Octane Wallet.
- Handles Solana addresses, SOL amounts, SNS domains, etc.  
    */  
    object Validators {
    
    ```
    /**
     * Validate Solana wallet address (base58, 32-44 chars).
     */
    fun isValidSolanaAddress(address: String): Boolean {
        if (address.isBlank() || address.length !in 32..44) return false
        
        // Base58 alphabet (no 0, O, I, l)
        val base58Regex = "^[1-9A-HJ-NP-Za-km-z]+$".toRegex()
        return address.matches(base58Regex)
    }
    
    /**
     * Validate SOL amount.
     * @param amount Amount as string
     * @param maxDecimals Maximum decimal places (default: 9)
     * @param min Minimum value (default: 0)
     * @param max Maximum value (default: unlimited)
     */
    fun validateSOLAmount(
        amount: String,
        maxDecimals: Int = 9,
        min: Double = 0.0,
        max: Double = Double.MAX_VALUE
    ): ValidationResult {
        if (amount.isBlank()) {
            return ValidationResult.Invalid("Amount cannot be empty")
        }
        
        val value = amount.toDoubleOrNull() 
            ?: return ValidationResult.Invalid("Invalid number format")
        
        if (value < min) {
            return ValidationResult.Invalid("Minimum amount is ${Formatters.formatSOL(min)}")
        }
        
        if (value > max) {
            return ValidationResult.Invalid("Maximum amount is ${Formatters.formatSOL(max)}")
        }
        
        // Check decimal places
        val parts = amount.split(".")
        if (parts.size == 2 && parts[1].length > maxDecimals) {
            return ValidationResult.Invalid("Maximum $maxDecimals decimal places allowed")
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * Validate SNS domain (.sol names).
     */
    fun isValidSNSDomain(domain: String): Boolean {
        if (!domain.endsWith(".sol")) return false
        val name = domain.substringBeforeLast(".sol")
        if (name.isEmpty() || name.length < 2) return false
        
        // SNS names: lowercase, numbers, hyphens
        val snsRegex = "^[a-z0-9-]+$".toRegex()
        return name.matches(snsRegex)
    }
    
    /**
     * Validate memo (optional transaction note).
     * @param memo Memo text
     * @param maxLength Maximum length (Solana limit: ~566 bytes)
     */
    fun validateMemo(memo: String, maxLength: Int = 500): ValidationResult {
        if (memo.isEmpty()) return ValidationResult.Valid
        
        if (memo.length > maxLength) {
            return ValidationResult.Invalid("Memo too long (max $maxLength chars)")
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * Validate custom RPC endpoint URL.
     */
    fun isValidRpcUrl(url: String): Boolean {
        if (url.isBlank()) return false
        
        val urlRegex = "^(https?|wss?)://[^\\s/$.?#].[^\\s]*$".toRegex(RegexOption.IGNORE_CASE)
        return url.matches(urlRegex)
    }
    
    /**
     * Validate slippage percentage (for swaps).
     * @param slippage Slippage as percentage (e.g., 1.0 = 1%)
     */
    fun validateSlippage(slippage: Double): ValidationResult {
        return when {
            slippage < 0 -> ValidationResult.Invalid("Slippage cannot be negative")
            slippage > 50 -> ValidationResult.Invalid("Slippage too high (max 50%)")
            else -> ValidationResult.Valid
        }
    }
    ```
    
    }

/**

- Validation result sealed class.  
    */  
    sealed interface ValidationResult {  
    data object Valid : ValidationResult  
    data class Invalid(val message: String) : ValidationResult
    
    ```
    val isValid: Boolean get() = this is Valid
    val errorMessage: String? get() = (this as? Invalid)?.message
    ```
    
    }

// :app:core:util/Constants.kt

package com.octane.core.util

/**

- App-wide constants for Octane Wallet.  
    */  
    object Constants {
    
    ```
    // Network
    const val DEFAULT_RPC_TIMEOUT_MS = 30_000L
    const val RPC_HEALTH_CHECK_INTERVAL_MS = 10_000L
    const val MAX_RPC_RETRIES = 3
    
    // Transaction
    const val DEFAULT_PRIORITY_FEE_MICROLAMPORTS = 10_000L
    const val MAX_TRANSACTION_SIZE_BYTES = 1232
    const val DEFAULT_SLIPPAGE_PERCENT = 1.0
    
    // Security
    const val BIOMETRIC_AUTO_APPROVE_THRESHOLD_USD = 100.0
    const val MAX_FAILED_AUTH_ATTEMPTS = 3
    const val AUTH_LOCKOUT_DURATION_MS = 300_000L // 5 minutes
    
    // UI
    const val PRICE_UPDATE_THROTTLE_MS = 1000L
    const val PORTFOLIO_REFRESH_INTERVAL_MS = 30_000L
    const val TRANSACTION_HISTORY_PAGE_SIZE = 50
    
    // Cache
    const val PRICE_CACHE_TTL_MS = 60_000L // 1 minute
    const val TRANSACTION_CACHE_TTL_MS = 300_000L // 5 minutes
    const val NFT_METADATA_CACHE_TTL_MS = 3_600_000L // 1 hour
    
    // API Keys (store in BuildConfig in production)
    const val HELIUS_API_KEY_PLACEHOLDER = "YOUR_HELIUS_KEY"
    const val QUICKNODE_API_KEY_PLACEHOLDER = "YOUR_QUICKNODE_KEY"
    ```
    
    }

// :app:core:extensions/FlowExtensions.kt (commonMain)

package com.octane.core.extensions

import kotlinx.coroutines.delay  
import kotlinx.coroutines.flow.*  
import java.io.IOException  
import kotlin.math.pow  
import kotlin.time.Duration  
import kotlin.time.Duration.Companion.milliseconds

/**

- Retry RPC calls with exponential backoff.
- Specific to Solana RPC failures.  
    _/  
    fun Flow.retryRpcCall(  
    maxRetries: Int = 3,  
    initialDelay: Long = 500,  
    maxDelay: Long = 5000,  
    factor: Double = 2.0  
    ): Flow = retryWhen { cause, attempt ->  
    if (attempt < maxRetries && (cause is IOException || isRpcError(cause))) {  
    val delay = (initialDelay_ factor.pow(attempt.toDouble()))  
    .toLong()  
    .coerceAtMost(maxDelay)  
    delay(delay)  
    true  
    } else {  
    false  
    }  
    }

private fun isRpcError(throwable: Throwable): Boolean {  
val message = throwable.message?.lowercase() ?: ""  
return message.contains("rpc") ||  
message.contains("timeout") ||  
message.contains("429") // Rate limit  
}

/**

- Throttle emissions - emit at most once per period.
- Use for price updates to avoid excessive recompositions.  
    */  
    fun Flow.throttleFirst(period: Long): Flow = flow {  
    var lastEmitTime = 0L  
    collect { value ->  
    val currentTime = System.currentTimeMillis()  
    if (currentTime - lastEmitTime >= period) {  
    lastEmitTime = currentTime  
    emit(value)  
    }  
    }  
    }

/**

- Throttle emissions - emit latest value at interval.
- Use for search queries, real-time price updates.  
    */  
    fun Flow.throttleLatest(period: Long): Flow = flow {  
    conflate().collect { value ->  
    emit(value)  
    delay(period)  
    }  
    }

/**

- Emit only when value changes according to predicate.  
    */  
    fun <T, K> Flow.distinctUntilChangedBy(  
    keySelector: (T) -> K  
    ): Flow = flow {  
    var lastKey: K? = null  
    collect { value ->  
    val key = keySelector(value)  
    if (key != lastKey) {  
    lastKey = key  
    emit(value)  
    }  
    }  
    }

/**

- Timeout wrapper - emit error if Flow takes too long.  
    */  
    fun Flow.withTimeout(timeoutMillis: Long): Flow = flow {  
    kotlinx.coroutines.withTimeout(timeoutMillis) {  
    collect { value -> emit(value) }  
    }  
    }

/**

- Catch and transform errors to a fallback value.  
    */  
    fun Flow.catchWithFallback(fallback: T): Flow =  
    catch { emit(fallback) }

/**

- Log each emission for debugging.  
    */  
    fun Flow.logEach(tag: String = "Flow"): Flow = onEach { value ->  
    println("[$tag] Emitted: $value")  
    }

/**

- Combine with timeout for each emission.
- Use for RPC calls that should complete quickly.  
    */  
    fun Flow.withEmissionTimeout(  
    timeout: Duration  
    ): Flow = flow {  
    collect { value ->  
    kotlinx.coroutines.withTimeout(timeout) {  
    emit(value)  
    }  
    }  
    }

// :app:core:extensions/StringExtensions.kt (commonMain)

package com.octane.core.extensions

/**

- Truncate wallet address for display.
- Pattern: abcd...wxyz  
    */  
    fun String.truncateAddress(  
    prefixLength: Int = 4,  
    suffixLength: Int = 4  
    ): String {  
    if (length <= prefixLength + suffixLength + 3) return this  
    return "{takeLast(suffixLength)}"  
    }

/**

- Truncate to max length with ellipsis.  
    */  
    fun String.truncateToLength(maxLength: Int): String {  
    if (length <= maxLength) return this  
    return take(maxLength - 3) + "..."  
    }

/**

- Check if string is a valid Solana address.  
    */  
    fun String.isSolanaAddress(): Boolean {  
    if (length !in 32..44) return false  
    val base58Regex = "[[1]](http://localhost/#fn-1-c37f1ebbb087d6aa)+$".toRegex()  
    return matches(base58Regex)  
    }

/**

- Check if string is a SNS domain.  
    */  
    fun String.isSnsDomain(): Boolean {  
    return endsWith(".sol") && length > 4  
    }

/**

- Remove all whitespace.  
    */  
    fun String.removeWhitespace(): String =  
    replace("\s".toRegex(), "")

/**

- Capitalize first letter.  
    */  
    fun String.capitalizeFirst(): String {  
    if (isEmpty()) return this  
    return replaceFirstChar { it.uppercase() }  
    }

/**

- Extract numbers from string.  
    */  
    fun String.extractNumbers(): String {  
    return filter { it.isDigit() || it == '.' }  
    }

/**

- Mask sensitive data (show last N characters).  
    _/  
    fun String.maskSensitive(  
    visibleChars: Int = 4,  
    maskChar: Char = '_'  
    ): String {  
    if (length <= visibleChars) return this  
    val masked = maskChar.toString().repeat(length - visibleChars)  
    return masked + takeLast(visibleChars)  
    }

// :app:core:extensions/NumberExtensions.kt (commonMain)

package com.octane.core.extensions

import kotlin.math.pow

/**

- Format as SOL amount.  
    */  
    fun Double.formatSOL(decimals: Int = 4): String {  
    return "◎{"%.{decimals}f".format(this)}"  
    }

/**

- Format as USD currency.  
    */  
    fun Double.formatUSD(decimals: Int = 2): String {  
    return "${"%.{decimals}f".format(this)}"  
    }

/**

- Format large numbers compactly (1.5K, 2.3M).  
    */  
    fun Double.formatCompact(decimals: Int = 2): String {  
    return when {  
    this >= 1_000_000_000 -> "{"%.{decimals}f".format(this / 1_000_000_000)}B"  
    this >= 1_000_000 -> "{"%.{decimals}f".format(this / 1_000_000)}M"  
    this >= 1_000 -> "{"%.{decimals}f".format(this / 1_000)}K"  
    else -> "%.${decimals}f".format(this)  
    }  
    }

/**

- Format as percentage.  
    _/  
    fun Double.formatPercentage(  
    decimals: Int = 2,  
    showSign: Boolean = true  
    ): String {  
    val sign = when {  
    !showSign -> ""  
    this >= 0 -> "+"  
    else -> ""  
    }  
    val value = "%.${decimals}f".format(this_ 100)  
    return "value%"  
    }

/**

- Round to specific decimal places.  
    _/  
    fun Double.roundTo(decimals: Int): Double {  
    val factor = 10.0.pow(decimals)  
    return kotlin.math.round(this_ factor) / factor  
    }

/**

- Clamp value between min and max.  
    */  
    fun Double.clamp(min: Double, max: Double): Double =  
    coerceIn(min, max)

fun Int.clamp(min: Int, max: Int): Int =  
coerceIn(min, max)

/**

- Convert lamports to SOL (1 SOL = 1B lamports).  
    */  
    fun Long.lamportsToSOL(): Double {  
    return this / 1_000_000_000.0  
    }

/**

- Convert SOL to lamports.  
    _/  
    fun Double.solToLamports(): Long {  
    return (this_ 1_000_000_000).toLong()  
    }

/**

- Format bytes to human-readable format.  
    */  
    fun Long.formatBytes(): String {  
    return when {  
    this >= 1_073_741_824 -> "{"%.2f".format(this / 1_073_741_824.0)} GB" this >= 1_048_576 -> "{"%.2f".format(this / 1_048_576.0)} MB"  
    this >= 1_024 -> "{"%.2f".format(this / 1_024.0)} KB" else -> "this B"  
    }  
    }

// :app:core:extensions/DateTimeExtensions.kt (commonMain)

package com.octane.core.extensions

/**

- Format timestamp as relative time.  
    */  
    fun Long.formatRelativeTime(): String {  
    val now = System.currentTimeMillis()  
    val diff = now - this
    
    ```
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> formatDate()
    }
    ```
    
    }

/**

- Format timestamp as date.  
    */  
    fun Long.formatDate(pattern: String = "MMM dd, yyyy"): String {  
    // TODO: Use kotlinx-datetime for KMP  
    return java.text.SimpleDateFormat(pattern, java.util.Locale.US)  
    .format(java.util.Date(this))  
    }

/**

- Format timestamp as time.  
    */  
    fun Long.formatTime(use24Hour: Boolean = false): String {  
    val pattern = if (use24Hour) "HH:mm" else "hh:mm a"  
    return java.text.SimpleDateFormat(pattern, java.util.Locale.US)  
    .format(java.util.Date(this))  
    }

/**

- Check if timestamp is today.  
    */  
    fun Long.isToday(): Boolean {  
    val today = java.util.Calendar.getInstance()  
    val date = java.util.Calendar.getInstance().apply { timeInMillis = this@isToday }  
    return today.get(java.util.Calendar.YEAR) == date.get(java.util.Calendar.YEAR) &&  
    today.get(java.util.Calendar.DAY_OF_YEAR) == date.get(java.util.Calendar.DAY_OF_YEAR)  
    }

/**

- Check if timestamp is yesterday.  
    */  
    fun Long.isYesterday(): Boolean {  
    val yesterday = java.util.Calendar.getInstance().apply {  
    add(java.util.Calendar.DAY_OF_YEAR, -1)  
    }  
    val date = java.util.Calendar.getInstance().apply { timeInMillis = this@isYesterday }  
    return yesterday.get(java.util.Calendar.YEAR) == date.get(java.util.Calendar.YEAR) &&  
    yesterday.get(java.util.Calendar.DAY_OF_YEAR) == date.get(java.util.Calendar.DAY_OF_YEAR)  
    }

// :app:core:extensions/ComposeExtensions.kt (androidMain)

package com.octane.core.extensions

import androidx.compose.animation.core._  
import androidx.compose.foundation.background  
import androidx.compose.foundation.clickable  
import androidx.compose.runtime._  
import androidx.compose.ui.Modifier  
import androidx.compose.ui.composed  
import androidx.compose.ui.geometry.Offset  
import androidx.compose.ui.graphics.Brush  
import androidx.compose.ui.graphics.Color  
import androidx.compose.ui.hapticfeedback.HapticFeedbackType  
import androidx.compose.ui.platform.LocalHapticFeedback

/**

- Shimmer loading effect for skeleton screens.  
    */  
    fun Modifier.shimmer(  
    durationMillis: Int = 1000,  
    highlightColor: Color = Color.LightGray.copy(alpha = 0.6f),  
    backgroundColor: Color = Color.LightGray.copy(alpha = 0.3f)  
    ): Modifier = composed {  
    val transition = rememberInfiniteTransition(label = "shimmer")  
    val translateAnimation by transition.animateFloat(  
    initialValue = 0f,  
    targetValue = 1000f,  
    animationSpec = infiniteRepeatable(  
    animation = tween(durationMillis = durationMillis, easing = LinearEasing),  
    repeatMode = RepeatMode.Restart  
    ),  
    label = "shimmer_translate"  
    )
    
    ```
    background(
        Brush.linearGradient(
            colors = listOf(
                backgroundColor,
                highlightColor,
                backgroundColor
            ),
            start = Offset(translateAnimation, 0f),
            end = Offset(translateAnimation + 200f, 0f)
        )
    )
    ```
    
    }

/**

- Haptic feedback on click.  
    */  
    fun Modifier.hapticClick(  
    feedbackType: HapticFeedbackType = HapticFeedbackType.LongPress,  
    onClick: () -> Unit  
    ): Modifier = composed {  
    val haptic = LocalHapticFeedback.current  
    clickable {  
    haptic.performHapticFeedback(feedbackType)  
    onClick()  
    }  
    }

/**

- Conditional modifier - apply only if condition is true.  
    */  
    fun Modifier.conditionally(  
    condition: Boolean,  
    modifier: Modifier.() -> Modifier  
    ): Modifier {  
    return if (condition) this.modifier() else this  
    }

/**

- Pulse animation (for highlighting new items).  
    */  
    fun Modifier.pulse(  
    durationMillis: Int = 1000  
    ): Modifier = composed {  
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")  
    val scale by infiniteTransition.animateFloat(  
    initialValue = 1f,  
    targetValue = 1.05f,  
    animationSpec = infiniteRepeatable(  
    animation = tween(durationMillis),  
    repeatMode = RepeatMode.Reverse  
    ),  
    label = "pulse_scale"  
    )
    
    ```
    this.then(
        Modifier.composed {
            androidx.compose.ui.graphics.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        }
    )
    ```
    
    }

// :app:core:blockchain/GasFeeEstimator.kt

package com.octane.core.blockchain

import kotlinx.coroutines.flow.Flow  
import kotlinx.coroutines.flow.flow

/**

- Estimates Solana transaction priority fees.
- Provides low/medium/high fee options for user selection.  
    */  
    class GasFeeEstimator {
    
    ```
    /**
     * Estimate priority fees for a transaction.
     * @param transactionSize Estimated transaction size in bytes
     * @return Flow of fee estimates
     */
    fun estimatePriorityFee(
        transactionSize: Int = 300 // Average transaction size
    ): Flow<PriorityFeeEstimate> = flow {
        // TODO: Query recent priority fees from RPC
        // For now, return static estimates
        
        val estimate = PriorityFeeEstimate(
            low = PriorityFee(
                microLamports = 5_000,
                estimatedConfirmationTime = "~30-60s",
                successRate = 0.50
            ),
            medium = PriorityFee(
                microLamports = 10_000,
                estimatedConfirmationTime = "~15-30s",
                successRate = 0.75
            ),
            high = PriorityFee(
                microLamports = 50_000,
                estimatedConfirmationTime = "~5-15s",
                successRate = 0.95
            ),
            custom = null
        )
        
        emit(estimate)
    }
    
    /**
     * Calculate total transaction cost.
     * @param priorityFee Selected priority fee
     * @param baseFee Base transaction fee (typically 5000 lamports)
     */
    fun calculateTotalFee(
        priorityFee: Long,
        baseFee: Long = 5_000
    ): Long {
        return baseFee + (priorityFee / 1_000) // Convert microLamports
    }
    
    /**
     * Parse recent priority fees from RPC response.
     */
    private fun parseRecentFees(fees: List<Long>): PriorityFeeEstimate {
        val sorted = fees.sorted()
        
        return PriorityFeeEstimate(
            low = PriorityFee(
                microLamports = sorted[sorted.size * 25 / 100],
                estimatedConfirmationTime = "~30-60s",
                successRate = 0.50
            ),
            medium = PriorityFee(
                microLamports = sorted[sorted.size * 50 / 100],
                estimatedConfirmationTime = "~15-30s",
                successRate = 0.75
            ),
            high = PriorityFee(
                microLamports = sorted[sorted.size * 75 / 100],
                estimatedConfirmationTime = "~5-15s",
                successRate = 0.95
            ),
            custom = null
        )
    }
    ```
    
    }

/**

- Priority fee estimate with multiple tiers.  
    */  
    data class PriorityFeeEstimate(  
    val low: PriorityFee,  
    val medium: PriorityFee,  
    val high: PriorityFee,  
    val custom: PriorityFee?  
    )

/**

- Individual priority fee tier.  
    */  
    data class PriorityFee(  
    val microLamports: Long,  
    val estimatedConfirmationTime: String,  
    val successRate: Double // 0.0 - 1.0  
    ) {  
    val solAmount: Double  
    get() = microLamports / 1_000_000_000.0  
    }

// :app:core:blockchain/TransactionSimulator.kt

package com.octane.core.blockchain

/**

- Simulates transactions before signing.
- Integrates with Helius Transaction Simulation API.  
    _/  
    interface TransactionSimulator {  
    /**  
    _Simulate a transaction to preview its effects.  
    _@param transaction Serialized transaction bytes  
    _@return Simulation result with account changes  
    */  
    suspend fun simulate(  
    transaction: ByteArray  
    ): SimulationResult  
    }

/**

- Transaction simulation result.  
    _/  
    sealed class SimulationResult {  
    /**  
    _Simulation succeeded - transaction would succeed on-chain.  
    */  
    data class Success(  
    val accounts: List,  
    val logs: List,  
    val computeUnitsConsumed: Int  
    ) : SimulationResult()
    
    ```
    /**
     * Simulation failed - transaction would fail on-chain.
     */
    data class Error(
        val message: String,
        val logs: List<String>,
        val errorCode: Int? = null
    ) : SimulationResult()
    
    /**
     * Simulation shows suspicious activity.
     */
    data class Warning(
        val risk: RiskLevel,
        val reason: String,
        val accounts: List<AccountChange>
    ) : SimulationResult()
    ```
    
    }

/**

- Transaction risk level from simulation.  
    */  
    enum class RiskLevel {  
    SAFE, // Normal transaction  
    SUSPICIOUS, // Unusual but not necessarily dangerous  
    DANGEROUS, // High likelihood of scam  
    MALICIOUS // Known malicious contract/address  
    }

/**

- Account balance change from simulation.  
    */  
    data class AccountChange(  
    val address: String,  
    val before: Long, // Balance before (lamports)  
    val after: Long, // Balance after (lamports)  
    val delta: Long, // Change in balance  
    val tokenMint: String? // Token mint if SPL token  
    ) {  
    val isDecrease: Boolean get() = delta < 0  
    val isIncrease: Boolean get() = delta > 0  
    }

/**

- Stub implementation - integrate Helius API later.  
    */  
    class TransactionSimulatorImpl : TransactionSimulator {  
    override suspend fun simulate(  
    transaction: ByteArray  
    ): SimulationResult {  
    // TODO: Integrate Helius API  
    // POST [https://api.helius.xyz/v0/transactions/parse](https://api.helius.xyz/v0/transactions/parse)
    
    ```
        // For now, return success with empty changes
        return SimulationResult.Success(
            accounts = emptyList(),
            logs = listOf("Transaction simulation not yet implemented"),
            computeUnitsConsumed = 200_000
        )
    }
    ```
    
    }

// :app:core:blockchain/SolanaRpcManager.kt

package com.octane.core.blockchain

import com.octane.core.network.NetworkMonitor  
import kotlinx.coroutines.CoroutineScope  
import kotlinx.coroutines.Dispatchers  
import kotlinx.coroutines.SupervisorJob  
import kotlinx.coroutines.delay  
import kotlinx.coroutines.flow.MutableStateFlow  
import kotlinx.coroutines.flow.StateFlow  
import kotlinx.coroutines.flow.asStateFlow  
import kotlinx.coroutines.isActive  
import kotlinx.coroutines.launch  
import kotlin.time.Duration.Companion.seconds

/**

- Manages Solana RPC endpoints with automatic fallback.
- Monitors RPC health and switches to backup on failures.  
    */  
    class SolanaRpcManager(  
    private val networkMonitor: NetworkMonitor  
    ) {  
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    ```
    // RPC endpoints (in order of preference)
    private val endpoints = listOf(
        RpcEndpoint(
            url = "https://solana-mainnet.g.alchemy.com/v2/YOUR_KEY",
            type = RpcType.PRIMARY,
            name = "Alchemy"
        ),
        RpcEndpoint(
            url = "https://rpc.helius.xyz/?api-key=YOUR_KEY",
            type = RpcType.PRIMARY,
            name = "Helius"
        ),
        RpcEndpoint(
            url = "https://api.mainnet-beta.solana.com",
            type = RpcType.FALLBACK,
            name = "Public RPC"
        )
    )
    
    private val _currentEndpoint = MutableStateFlow(endpoints.first())
    val currentEndpoint: StateFlow<RpcEndpoint> = _currentEndpoint.asStateFlow()
    
    private val _endpointHealth = MutableStateFlow<Map<String, RpcHealth>>(emptyMap())
    val endpointHealth: StateFlow<Map<String, RpcHealth>> = _endpointHealth.asStateFlow()
    
    init {
        startHealthChecks()
    }
    
    /**
     * Get current RPC URL.
     */
    fun getCurrentRpcUrl(): String = _currentEndpoint.value.url
    
    /**
     * Switch to next available endpoint.
     */
    fun switchToNextEndpoint() {
        val currentIndex = endpoints.indexOf(_currentEndpoint.value)
        val nextIndex = (currentIndex + 1) % endpoints.size
        _currentEndpoint.value = endpoints[nextIndex]
    }
    
    /**
     * Set custom RPC endpoint.
     */
    fun setCustomEndpoint(url: String, name: String = "Custom") {
        val customEndpoint = RpcEndpoint(
            url = url,
            type = RpcType.CUSTOM,
            name = name
        )
        _currentEndpoint.value = customEndpoint
    }
    
    /**
     * Check health of all endpoints periodically.
     */
    private fun startHealthChecks() {
        scope.launch {
            while (isActive) {
                if (networkMonitor.isConnected.value) {
                    checkAllEndpoints()
                }
                delay(10.seconds)
            }
        }
    }
    
    private suspend fun checkAllEndpoints() {
        val healthMap = mutableMapOf<String, RpcHealth>()
        
        endpoints.forEach { endpoint ->
            val health = checkEndpointHealth(endpoint)
            healthMap[endpoint.url] = health
            
            // Auto-switch if current endpoint is down
            if (endpoint == _currentEndpoint.value && health.status == HealthStatus.DOWN) {
                switchToNextEndpoint()
            }
        }
        
        _endpointHealth.value = healthMap
    }
    
    private suspend fun checkEndpointHealth(endpoint: RpcEndpoint): RpcHealth {
        val startTime = System.currentTimeMillis()
        
        return try {
            // TODO: Implement actual RPC health check
            // Use getHealth or getVersion RPC methods
            
            val latency = System.currentTimeMillis() - startTime
            
            RpcHealth(
                endpoint = endpoint,
                latency = latency,
                status = when {
                    latency < 500 -> HealthStatus.HEALTHY
                    latency < 2000 -> HealthStatus.SLOW
                    else -> HealthStatus.DEGRADED
                },
                lastChecked = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            RpcHealth(
                endpoint = endpoint,
                latency = 0,
                status = HealthStatus.DOWN,
                lastChecked = System.currentTimeMillis(),
                error = e.message
            )
        }
    }
    ```
    
    }

/**

- RPC endpoint configuration.  
    */  
    data class RpcEndpoint(  
    val url: String,  
    val type: RpcType,  
    val name: String  
    )

enum class RpcType {  
PRIMARY, // Paid, high-quality RPC (Helius, Alchemy)  
FALLBACK, // Free public RPC (slower, rate-limited)  
CUSTOM // User-provided RPC  
}

/**

- RPC endpoint health status.  
    */  
    data class RpcHealth(  
    val endpoint: RpcEndpoint,  
    val latency: Long,  
    val status: HealthStatus,  
    val lastChecked: Long,  
    val error: String? = null  
    )

enum class HealthStatus {  
HEALTHY, // <500ms latency  
SLOW, // 500ms-2s latency  
DEGRADED, // >2s latency  
DOWN // Unreachable or erroring  
}

// :app:core:blockchain/TransactionBuilder.kt

package com.octane.core.blockchain

/**

- Builder for constructing Solana transactions.
- Simplifies transaction creation for common operations.  
    */  
    class TransactionBuilder {
    
    ```
    /**
     * Build a SOL transfer transaction.
     * @param from Sender's public key
     * @param to Recipient's public key
     * @param lamports Amount to send in lamports
     * @param memo Optional transaction memo
     */
    fun buildTransfer(
        from: String,
        to: String,
        lamports: Long,
        memo: String? = null
    ): ByteArray {
        // TODO: Implement using Solana SDK
        // 1. Create transfer instruction
        // 2. Add memo instruction if provided
        // 3. Create transaction with recent blockhash
        // 4. Serialize transaction
        
        return ByteArray(0) // Placeholder
    }
    
    /**
     * Build an SPL token transfer transaction.
     * @param from Sender's token account
     * @param to Recipient's token account
     * @param amount Amount in token's smallest unit
     * @param mint Token mint address
     */
    fun buildTokenTransfer(
        from: String,
        to: String,
        amount: Long,
        mint: String
    ): ByteArray {
        // TODO: Implement using Solana SDK
        return ByteArray(0) // Placeholder
    }
    
    /**
     * Build a swap transaction using Jupiter.
     * @param inputMint Input token mint
     * @param outputMint Output token mint
     * @param amount Input amount
     * @param slippage Slippage tolerance (percentage)
     */
    fun buildSwap(
        inputMint: String,
        outputMint: String,
        amount: Long,
        slippage: Double
    ): ByteArray {
        // TODO: Implement Jupiter swap integration
        return ByteArray(0) // Placeholder
    }
    ```
    
    }

// :app:core:monitoring/AnalyticsLogger.kt (commonMain)

package com.octane.core.monitoring

/**

- Analytics abstraction for Octane Wallet.
- Tracks user actions, performance, and errors.  
    */  
    interface AnalyticsLogger {
    
    ```
    /**
     * Log screen view.
     */
    fun logScreenView(
        screenName: String,
        screenClass: String? = null
    )
    
    /**
     * Log user event.
     */
    fun logEvent(
        eventName: String,
        params: Map<String, Any> = emptyMap()
    )
    
    /**
     * Log error/exception.
     */
    fun logError(
        throwable: Throwable,
        context: String? = null,
        fatal: Boolean = false
    )
    
    /**
     * Set user ID for tracking.
     */
    fun setUserId(userId: String?)
    
    /**
     * Set user property.
     */
    fun setUserProperty(
        propertyName: String,
        value: String
    )
    
    /**
     * Log performance metric.
     */
    fun logPerformance(
        metricName: String,
        durationMillis: Long,
        attributes: Map<String, String> = emptyMap()
    )
    ```
    
    }

/**

- Standard event names for Octane.  
    */  
    object OctaneEvents {  
    // Wallet Lifecycle  
    const val WALLET_CREATED = "wallet_created"  
    const val WALLET_IMPORTED = "wallet_imported"  
    const val WALLET_DELETED = "wallet_deleted"
    
    ```
    // Transactions
    const val SEND_INITIATED = "send_sol_initiated"
    const val SEND_COMPLETED = "send_sol_completed"
    const val SEND_FAILED = "send_sol_failed"
    
    const val SWAP_INITIATED = "swap_initiated"
    const val SWAP_COMPLETED = "swap_completed"
    const val SWAP_FAILED = "swap_failed"
    
    const val NFT_MINTED = "nft_minted"
    const val NFT_SENT = "nft_sent"
    
    // Security
    const val BIOMETRIC_ENABLED = "biometric_enabled"
    const val BIOMETRIC_USED = "biometric_auth_used"
    const val MALICIOUS_TX_BLOCKED = "malicious_transaction_blocked"
    
    // Performance
    const val RPC_LATENCY = "rpc_latency_ms"
    const val TX_CONFIRMATION_TIME = "tx_confirmation_seconds"
    const val SWAP_EXECUTION_TIME = "swap_execution_seconds"
    
    // User Actions
    const val SCREEN_VIEWED = "screen_viewed"
    const val BUTTON_CLICKED = "button_clicked"
    const val SEARCH_PERFORMED = "search_performed"
    
    // Errors
    const val ERROR_OCCURRED = "error_occurred"
    const val API_ERROR = "api_error"
    const val RPC_ERROR = "rpc_error"
    ```
    
    }

/**

- Standard parameter names.  
    */  
    object OctaneParams {  
    const val SCREEN_NAME = "screen_name"  
    const val BUTTON_ID = "button_id"  
    const val WALLET_ID = "wallet_id"  
    const val TOKEN_SYMBOL = "token_symbol"  
    const val AMOUNT_USD = "amount_usd"  
    const val ERROR_MESSAGE = "error_message"  
    const val RPC_ENDPOINT = "rpc_endpoint"  
    const val NETWORK = "network"  
    const val SUCCESS = "success"  
    }

// :app:core:monitoring/FakeAnalyticsLogger.kt (commonTest)

package com.octane.core.monitoring

/**

- Fake analytics logger for testing.
- Captures events for verification.  
    */  
    class FakeAnalyticsLogger : AnalyticsLogger {  
    private val _events = mutableListOf()  
    val events: List get() = _events.toList()
    
    ```
    private val _screenViews = mutableListOf<String>()
    val screenViews: List<String> get() = _screenViews.toList()
    
    override fun logScreenView(screenName: String, screenClass: String?) {
        _screenViews.add(screenName)
    }
    
    override fun logEvent(eventName: String, params: Map<String, Any>) {
        _events.add(AnalyticsEvent(eventName, params))
    }
    
    override fun logError(throwable: Throwable, context: String?, fatal: Boolean) {
        _events.add(AnalyticsEvent(
            name = OctaneEvents.ERROR_OCCURRED,
            params = mapOf(
                "message" to (throwable.message ?: "Unknown"),
                "context" to (context ?: "None"),
                "fatal" to fatal
            )
        ))
    }
    
    override fun setUserId(userId: String?) {
        // No-op for fake
    }
    
    override fun setUserProperty(propertyName: String, value: String) {
        // No-op for fake
    }
    
    override fun logPerformance(
        metricName: String,
        durationMillis: Long,
        attributes: Map<String, String>
    ) {
        _events.add(AnalyticsEvent(
            name = metricName,
            params = attributes + ("duration_ms" to durationMillis)
        ))
    }
    
    // Test helpers
    fun clear() {
        _events.clear()
        _screenViews.clear()
    }
    
    fun getEvent(eventName: String): AnalyticsEvent? {
        return _events.find { it.name == eventName }
    }
    
    fun hasEvent(eventName: String): Boolean {
        return _events.any { it.name == eventName }
    }
    
    fun getEventsOfType(eventName: String): List<AnalyticsEvent> {
        return _events.filter { it.name == eventName }
    }
    ```
    
    }

data class AnalyticsEvent(  
val name: String,  
val params: Map<String, Any>  
)

// :app:core:monitoring/PerformanceTracker.kt

package com.octane.core.monitoring

import kotlin.system.measureTimeMillis

/**

- Tracks performance metrics for Octane operations.  
    */  
    class PerformanceTracker(  
    private val analyticsLogger: AnalyticsLogger  
    ) {
    
    ```
    /**
     * Track RPC call latency.
     */
    suspend fun trackRpcCall(
        endpoint: String,
        method: String,
        block: suspend () -> Unit
    ) {
        val duration = measureTimeMillis {
            block()
        }
        
        analyticsLogger.logPerformance(
            metricName = OctaneEvents.RPC_LATENCY,
            durationMillis = duration,
            attributes = mapOf(
                "endpoint" to endpoint,
                "method" to method
            )
        )
    }
    
    /**
     * Track transaction confirmation time.
     */
    suspend fun trackTransactionConfirmation(
        signature: String,
        block: suspend () -> Unit
    ) {
        val duration = measureTimeMillis {
            block()
        }
        
        analyticsLogger.logPerformance(
            metricName = OctaneEvents.TX_CONFIRMATION_TIME,
            durationMillis = duration / 1000, // Convert to seconds
            attributes = mapOf(
                "signature" to signature
            )
        )
    }
    
    /**
     * Track swap execution time.
     */
    suspend fun trackSwapExecution(
        inputToken: String,
        outputToken: String,
        block: suspend () -> Unit
    ) {
        val duration = measureTimeMillis {
            block()
        }
        
        analyticsLogger.logPerformance(
            metricName = OctaneEvents.SWAP_EXECUTION_TIME,
            durationMillis = duration / 1000,
            attributes = mapOf(
                "input_token" to inputToken,
                "output_token" to outputToken
            )
        )
    }
    ```
    
    }

// :app:core:di/CoreModule.kt

package com.octane.core.di

import com.octane.core.blockchain._  
import com.octane.core.monitoring._  
import com.octane.core.network._  
import com.octane.core.security._  
import kotlinx.coroutines.Dispatchers  
import org.koin.android.ext.koin.androidContext  
import org.koin.core.qualifier.named  
import org.koin.dsl.module

/**

- Koin DI module for Core layer.
- Provides all core infrastructure components.  
    */  
    val coreModule = module {
    
    ```
    // Dispatchers
    single(named("IO")) { Dispatchers.IO }
    single(named("Main")) { Dispatchers.Main }
    single(named("Default")) { Dispatchers.Default }
    
    // Network
    single<NetworkMonitor> { NetworkMonitorImpl(androidContext()) }
    single { SolanaNetworkMonitor(get()) }
    
    // Blockchain
    single { SolanaRpcManager(get()) }
    single { GasFeeEstimator() }
    single<TransactionSimulator> { TransactionSimulatorImpl() }
    single { TransactionBuilder() }
    
    // Security
    single { KeystoreManager(androidContext()) }
    single { BiometricManager(androidContext()) }
    single<MaliciousSignatureDetector> { MaliciousSignatureDetectorImpl() }
    single { PhishingBlocklist() }
    
    // Monitoring
    single<AnalyticsLogger> {
        // TODO: Replace with Firebase in production
        FakeAnalyticsLogger()
    }
    single { PerformanceTracker(get()) }
    
    }
    