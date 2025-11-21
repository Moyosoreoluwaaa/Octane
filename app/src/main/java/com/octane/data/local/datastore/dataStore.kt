package com.octane.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * User Preferences Store (DataStore implementation)
 *
 * Stores all app-wide settings, including UI, security, and domain-specific settings.
 */
interface UserPreferencesStore {
    // UI, Security, and Core Settings
    val currencyPreference: Flow<String>
    val privacyMode: Flow<Boolean>
    val biometricEnabled: Flow<Boolean>
    val autoLockTimeout: Flow<Int>
    val theme: Flow<String>
    val language: Flow<String>
    val testnetEnabled: Flow<Boolean>

    // Domain-Specific Settings
    val lastActiveWalletId: Flow<String?>
    val selectedChainId: Flow<String>
    val hideZeroBalances: Flow<Boolean>
    val sortBy: Flow<String>
    val rpcEndpoint: Flow<String>
    val customRpcUrl: Flow<String?>

    // Setters - Core
    suspend fun setCurrency(currency: String)
    suspend fun setPrivacyMode(enabled: Boolean)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setAutoLockTimeout(seconds: Int)
    suspend fun setTheme(theme: String)
    suspend fun setLanguage(language: String)
    suspend fun setTestnetEnabled(enabled: Boolean)

    // Setters - Domain
    suspend fun setLastActiveWalletId(walletId: String?)
    suspend fun setSelectedChainId(chainId: String)
    suspend fun setHideZeroBalances(hide: Boolean)
    suspend fun setSortBy(sortBy: String)
    suspend fun setRpcEndpoint(endpoint: String)
    suspend fun setCustomRpcUrl(url: String?)

    // Utility
    suspend fun clearAll()
}

/**
 * DataStore implementation
 */
class UserPreferencesStoreImpl(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesStore {

    /**
     * Preference keys for all settings.
     */
    private object PreferencesKeys {
        // UI, Security, and Core Settings
        val CURRENCY = stringPreferencesKey("currency")
        val PRIVACY_MODE = booleanPreferencesKey("privacy_mode")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val AUTO_LOCK_TIMEOUT = intPreferencesKey("auto_lock_timeout")
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val TESTNET_ENABLED = booleanPreferencesKey("testnet_enabled")

        // Domain-Specific Settings
        val LAST_ACTIVE_WALLET_ID = stringPreferencesKey("last_active_wallet_id")
        val SELECTED_CHAIN_ID = stringPreferencesKey("selected_chain_id")
        val HIDE_ZERO_BALANCES = booleanPreferencesKey("hide_zero_balances")
        val SORT_BY = stringPreferencesKey("sort_by")
        val RPC_ENDPOINT = stringPreferencesKey("rpc_endpoint")
        val CUSTOM_RPC_URL = stringPreferencesKey("custom_rpc_url")
    }

    // --- Flow Observables ---

    // UI, Security, and Core Settings
    override val currencyPreference: Flow<String> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.CURRENCY] ?: "USD" }

    override val privacyMode: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.PRIVACY_MODE] == true }

    override val biometricEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.BIOMETRIC_ENABLED] == true }

    override val autoLockTimeout: Flow<Int> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.AUTO_LOCK_TIMEOUT] ?: 300 }

    override val theme: Flow<String> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.THEME] ?: "AUTO" }

    override val language: Flow<String> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.LANGUAGE] ?: "en" }

    override val testnetEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.TESTNET_ENABLED] == true }

    // Domain-Specific Settings
    override val lastActiveWalletId: Flow<String?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.LAST_ACTIVE_WALLET_ID] }

    override val selectedChainId: Flow<String> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.SELECTED_CHAIN_ID] ?: "solana" }

    override val hideZeroBalances: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.HIDE_ZERO_BALANCES] == true }

    override val sortBy: Flow<String> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.SORT_BY] ?: "VALUE_DESC" }

    override val rpcEndpoint: Flow<String> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.RPC_ENDPOINT] ?: "DEFAULT" }

    override val customRpcUrl: Flow<String?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.CUSTOM_RPC_URL] }


    // --- Suspend Setters ---

    // Core Setters
    override suspend fun setCurrency(currency: String) {
        dataStore.edit { it[PreferencesKeys.CURRENCY] = currency }
    }

    override suspend fun setPrivacyMode(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.PRIVACY_MODE] = enabled }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.BIOMETRIC_ENABLED] = enabled }
    }

    override suspend fun setAutoLockTimeout(seconds: Int) {
        dataStore.edit { it[PreferencesKeys.AUTO_LOCK_TIMEOUT] = seconds }
    }

    override suspend fun setTheme(theme: String) {
        dataStore.edit { it[PreferencesKeys.THEME] = theme }
    }

    override suspend fun setLanguage(language: String) {
        dataStore.edit { it[PreferencesKeys.LANGUAGE] = language }
    }

    override suspend fun setTestnetEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.TESTNET_ENABLED] = enabled }
    }

    // Domain Setters
    override suspend fun setSelectedChainId(chainId: String) {
        dataStore.edit { it[PreferencesKeys.SELECTED_CHAIN_ID] = chainId }
    }

    override suspend fun setHideZeroBalances(hide: Boolean) {
        dataStore.edit { it[PreferencesKeys.HIDE_ZERO_BALANCES] = hide }
    }

    override suspend fun setSortBy(sortBy: String) {
        dataStore.edit { it[PreferencesKeys.SORT_BY] = sortBy }
    }

    override suspend fun setRpcEndpoint(endpoint: String) {
        dataStore.edit { it[PreferencesKeys.RPC_ENDPOINT] = endpoint }
    }

    // Nullable Setters (Require remove logic)
    override suspend fun setLastActiveWalletId(walletId: String?) {
        dataStore.edit { preferences ->
            if (walletId != null) {
                preferences[PreferencesKeys.LAST_ACTIVE_WALLET_ID] = walletId
            } else {
                preferences.remove(PreferencesKeys.LAST_ACTIVE_WALLET_ID)
            }
        }
    }

    override suspend fun setCustomRpcUrl(url: String?) {
        dataStore.edit { preferences ->
            if (url != null) {
                preferences[PreferencesKeys.CUSTOM_RPC_URL] = url
            } else {
                preferences.remove(PreferencesKeys.CUSTOM_RPC_URL)
            }
        }
    }

    // Utility
    override suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}

/**
 * Koin Module for UserPreferencesStore setup
 */
val preferencesModule = module {
    // 1. Provide the DataStore instance
    single {
        androidContext().dataStore
    }

    // 2. Bind the implementation to the interface for DI
    single<UserPreferencesStore> {
        UserPreferencesStoreImpl(dataStore = get())
    }
}

/**
 * DataStore extension for Context
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "octane_preferences"
)