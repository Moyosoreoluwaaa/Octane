// app/core/data/local/datastore/UserPreferencesStore.kt

package com.octane.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore for user preferences.
 * Supports v0.7-v1.9 settings (currency, privacy, RPC, filters).
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesStore(private val context: Context) {
    
    // Keys
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
    
    // Flows
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
        .map { it[PreferenceKeys.RPC_ENDPOINT] ?: "DEFAULT" }
    
    val customRpcUrl: Flow<String?> = context.dataStore.data
        .map { it[PreferenceKeys.CUSTOM_RPC_URL] }
    
    val biometricEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[PreferenceKeys.BIOMETRIC_ENABLED] ?: false }
    
    // Setters
    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { it[PreferenceKeys.CURRENCY] = currency }
    }
    
    suspend fun setPrivacyMode(enabled: Boolean) {
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
    
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}