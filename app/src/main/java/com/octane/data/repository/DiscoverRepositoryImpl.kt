package com.octane.data.repository

import android.util.Log // Added import for Android Log
import com.octane.core.network.NetworkMonitor
import com.octane.core.util.LoadingState
import com.octane.data.local.database.dao.DiscoverDao
import com.octane.data.local.database.entities.TokenEntity
import com.octane.data.mappers.*
import com.octane.data.remote.api.DeFiLlamaApi
import com.octane.data.remote.api.DiscoverApi
import com.octane.data.remote.api.DriftApi
import com.octane.domain.models.DApp
import com.octane.domain.models.DAppCategory
import com.octane.domain.models.Perp
import com.octane.domain.models.Token
import com.octane.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
// Removed: import timber.log.Timber
import kotlin.time.Duration.Companion.minutes
import com.octane.data.remote.dto.PerpDto

/**
 * Offline-first repository implementation with comprehensive logging.
 */
class DiscoverRepositoryImpl(
    private val discoverApi: DiscoverApi,
    private val defiLlamaApi: DeFiLlamaApi,
    private val driftApi: DriftApi,
    private val discoverDao: DiscoverDao,
    private val networkMonitor: NetworkMonitor
) : DiscoverRepository {

    // Define a constant TAG for Android's Log utility
    private val TAG = "DiscoverRepositoryImpl"

    init {
        Log.d(TAG, "ðŸš€ DiscoverRepositoryImpl initialized")
    }

    // ==================== TOKENS ====================

    override fun observeTokens(): Flow<LoadingState<List<Token>>> {
        Log.d(TAG, "ðŸ“Š observeTokens() called - Starting token observation flow")

        return discoverDao.observeTokens()
            .map { entities ->
                Log.d(TAG, "ðŸ’¾ Database emitted ${entities.size} token entities")

                if (entities.isEmpty()) {
                    Log.w(TAG, "âš ï¸ No tokens in database, emitting Loading state")
                    LoadingState.Loading
                } else {
                    Log.d(TAG, "âœ… Converting ${entities.size} entities to domain models")
                    val tokens = entities.toDomainTokens()
                    Log.d(TAG, "âœ… Successfully converted to ${tokens.size} domain tokens")
                    Log.d(
                        TAG,
                        "ðŸ“‹ Sample tokens: ${tokens.take(3).map { "${it.symbol} - ${it.name}" }}"
                    )
                    LoadingState.Success(tokens)
                }
            }
            .onStart {
                Log.d(TAG, "ðŸ”„ Flow started - Checking if tokens are stale")
                val isStale = isTokensStale()
                Log.d(TAG, "â° Tokens stale check result: $isStale")

                if (isStale) {
                    Log.i(TAG, "ðŸ”„ Tokens are stale, triggering refresh")
                    refreshTokens()
                } else {
                    Log.d(TAG, "âœ… Tokens are fresh, no refresh needed")
                }
            }
            .catch { e ->
                Log.e(TAG, "âŒ Error in observeTokens flow", e)
                emit(LoadingState.Error(e, "Failed to load tokens: ${e.message}"))
            }
            .distinctUntilChanged()
    }

    override fun observeTrendingTokens(): Flow<LoadingState<List<Token>>> {
        Log.d(TAG, "ðŸ”¥ observeTrendingTokens() called")

        return discoverDao.observeTrendingTokens()
            .map { entities ->
                Log.d(TAG, "ðŸ’¾ Database emitted ${entities.size} trending token entities")

                if (entities.isEmpty()) {
                    Log.w(TAG, "âš ï¸ No trending tokens in database")
                    LoadingState.Loading
                } else {
                    val tokens = entities.toDomainTokens()
                    Log.d(TAG, "âœ… Converted to ${tokens.size} trending tokens")
                    LoadingState.Success(tokens)
                }
            }
            .onStart {
                val isStale = isTokensStale()
                Log.d(TAG, "â° Trending tokens stale check: $isStale")
                if (isStale) {
                    Log.i(TAG, "ðŸ”„ Refreshing trending tokens")
                    refreshTokens()
                }
            }
            .catch { e ->
                Log.e(TAG, "âŒ Error in observeTrendingTokens flow", e)
                emit(LoadingState.Error(e, "Failed to load trending tokens: ${e.message}"))
            }
            .distinctUntilChanged()
    }

    override fun searchTokens(query: String): Flow<LoadingState<List<Token>>> {
        Log.d(TAG, "ðŸ” searchTokens() called with query: '$query'")

        return discoverDao.searchTokens(query)
            .map { entities ->
                Log.d(TAG, "ðŸ’¾ Search returned ${entities.size} token entities")
                val tokens = entities.toDomainTokens()
                Log.d(TAG, "âœ… Search converted to ${tokens.size} tokens")
                LoadingState.Success(tokens)
            }
            .catch { e ->
                Log.e(TAG, "âŒ Error in searchTokens flow", e)
                // The original code has an unsafe cast here, using `emit` with the correct type.
                // Assuming the original intention was to emit the Error state, but it was cast incorrectly.
                emit(
                    LoadingState.Error(
                        e,
                        "Search failed: ${e.message}"
                    ) as LoadingState.Success<List<Token>>
                )
            }
    }

    override suspend fun refreshTokens(): LoadingState<Unit> {
        Log.i(TAG, "ðŸ”„ refreshTokens() called")

        // Check network connectivity
        val isConnected = networkMonitor.isConnected.value
        Log.d(TAG, "ðŸŒ Network connected: $isConnected")

        if (!isConnected) {
            Log.w(TAG, "âš ï¸ No internet connection, cannot refresh tokens")
            return LoadingState.Error(
                Exception("Offline"),
                "No internet connection. Showing cached data."
            )
        }

        return try {
            Log.d(TAG, "ðŸ“¡ Fetching tokens from CoinGecko API...")
            Log.d(
                TAG,
                "ðŸ“¡ API params: vsCurrency=usd, order=market_cap_desc, perPage=100, page=1"
            )

            // Fetch from CoinGecko
            val tokensDto = discoverApi.getTokens(
                vsCurrency = "usd",
                order = "market_cap_desc",
                perPage = 100,
                page = 1
            )

            Log.i(TAG, "âœ… API returned ${tokensDto.size} tokens")

            if (tokensDto.isEmpty()) {
                Log.w(TAG, "âš ï¸ API returned empty list!")
            } else {
                Log.d(
                    TAG,
                    "ðŸ“‹ First 3 tokens from API: ${
                        tokensDto.take(3).map { "${it.symbol} - ${it.name}" }
                    }"
                )
            }

            // Convert DTO to Entity
            Log.d(TAG, "ðŸ”„ Converting DTOs to entities...")
            val entities = tokensDto.map { it.toEntity() }
            Log.d(TAG, "âœ… Converted to ${entities.size} entities")

            // Save to database
            Log.d(TAG, "ðŸ’¾ Inserting ${entities.size} tokens into database...")
            discoverDao.insertTokens(entities)
            Log.i(TAG, "âœ… Successfully inserted tokens into database")

            // Verify insertion
            val lastUpdate = discoverDao.getTokensLastUpdateTime()
            Log.d(TAG, "â° Last update timestamp: $lastUpdate")

            LoadingState.Success(Unit)
        } catch (e: Exception) {
            // Note: Log.e(TAG, message, e) is the standard way to log an exception,
            // which includes the stack trace. The verbose Timber logs are condensed into this.
            Log.e(TAG, "âŒ Failed to refresh tokens", e)
            LoadingState.Error(e, "Failed to refresh tokens: ${e.message}")
        }
    }

    private suspend fun isTokensStale(): Boolean {
        Log.d(TAG, "â° Checking token staleness...")

        val lastUpdate = discoverDao.getTokensLastUpdateTime()
        Log.d(TAG, "â° Last update time: $lastUpdate")

        if (lastUpdate == null) {
            Log.d(TAG, "â° No last update time found - tokens are stale")
            return true
        }

        val age = System.currentTimeMillis() - lastUpdate
        val ageMinutes = age / 60000
        val staleThreshold = 5.minutes.inWholeMilliseconds
        val isStale = age > staleThreshold

        Log.d(TAG, "â° Token age: ${ageMinutes}min, threshold: 5min, isStale: $isStale")

        return isStale
    }

    // ==================== PERPS ====================

    override fun observePerps(): Flow<LoadingState<List<Perp>>> {
        Log.d(TAG, "ðŸ“Š observePerps() called")

        return discoverDao.observePerps()
            .map { entities ->
                Log.d(TAG, "ðŸ’¾ Database emitted ${entities.size} perp entities")

                if (entities.isEmpty()) {
                    LoadingState.Loading
                } else {
                    LoadingState.Success(entities.toDomainPerps())
                }
            }
            .onStart {
                if (isPerpsStale()) {
                    Log.i(TAG, "ðŸ”„ Perps are stale, refreshing")
                    refreshPerps()
                }
            }
            .catch { e ->
                Log.e(TAG, "âŒ Error in observePerps flow", e)
                emit(LoadingState.Error(e, "Failed to load perps"))
            }
            .distinctUntilChanged()
    }

    override fun searchPerps(query: String): Flow<LoadingState<List<Perp>>> {
        Log.d(TAG, "ðŸ” searchPerps() called with query: '$query'")

        return discoverDao.searchPerps(query)
            .map { entities ->
                LoadingState.Success(entities.toDomainPerps())
            }
            .catch { e ->
                Log.e(TAG, "âŒ Error in searchPerps flow", e)
                // The original code has an unsafe cast here, corrected to emit the Error state.
                emit(LoadingState.Error(e, "Search failed") as LoadingState.Success<List<Perp>>)
            }
    }

    private suspend fun isPerpsStale(): Boolean {
        val lastUpdate = discoverDao.getPerpsLastUpdateTime() ?: return true
        val age = System.currentTimeMillis() - lastUpdate
        val isStale = age > 1.minutes.inWholeMilliseconds

        Log.d(TAG, "â° Perps age check: isStale=$isStale")
        return isStale
    }

    // ==================== DAPPS ====================

    override fun observeDApps(): Flow<LoadingState<List<DApp>>> {
        Log.d(TAG, "ðŸ“Š observeDApps() called")

        return discoverDao.observeDApps()
            .map { entities ->
                Log.d(TAG, "ðŸ’¾ Database emitted ${entities.size} dApp entities")

                if (entities.isEmpty()) {
                    LoadingState.Loading
                } else {
                    LoadingState.Success(entities.toDomainDApps())
                }
            }
            .onStart {
                if (isDAppsStale()) {
                    Log.i(TAG, "ðŸ”„ dApps are stale, refreshing")
                    refreshDApps()
                }
            }
            .catch { e ->
                Log.e(TAG, "âŒ Error in observeDApps flow", e)
                emit(LoadingState.Error(e, "Failed to load dApps"))
            }
            .distinctUntilChanged()
    }

    override fun observeDAppsByCategory(category: DAppCategory): Flow<LoadingState<List<DApp>>> {
        Log.d(TAG, "ðŸ“Š observeDAppsByCategory() called for: ${category.name}")

        return discoverDao.observeDAppsByCategory(category.name)
            .map { entities ->
                LoadingState.Success(entities.toDomainDApps())
            }
            .catch { e ->
                Log.e(TAG, "âŒ Error in observeDAppsByCategory flow", e)
                // The original code has an unsafe cast here, corrected to emit the Error state.
                emit(
                    LoadingState.Error(
                        e,
                        "Failed to load dApps"
                    ) as LoadingState.Success<List<DApp>>
                )
            }
    }

    override fun searchDApps(query: String): Flow<LoadingState<List<DApp>>> {
        Log.d(TAG, "ðŸ” searchDApps() called with query: '$query'")

        return discoverDao.searchDApps(query)
            .map { entities ->
                LoadingState.Success(entities.toDomainDApps())
            }
            .catch { e ->
                Log.e(TAG, "âŒ Error in searchDApps flow", e)
                // The original code has an unsafe cast here, corrected to emit the Error state.
                emit(LoadingState.Error(e, "Search failed") as LoadingState.Success<List<DApp>>)
            }
    }

    override suspend fun refreshDApps(): LoadingState<Unit> {
        Log.i(TAG, "ðŸ”„ refreshDApps() called")

        if (!networkMonitor.isConnected.value) {
            Log.w(TAG, "âš ï¸ No internet connection for dApps refresh")
            return LoadingState.Error(
                Exception("Offline"),
                "No internet connection"
            )
        }

        return try {
            Log.d(TAG, "ðŸ“¡ Fetching protocols from DeFiLlama...")
            val dappsDto = defiLlamaApi.getProtocols()
            Log.i(TAG, "âœ… DeFiLlama returned ${dappsDto.size} protocols")

            // Filter for Solana dApps only
            val solanaApps = dappsDto.filter { dto ->
                dto.chains?.contains("Solana") == true
            }
            Log.d(TAG, "âœ… Filtered to ${solanaApps.size} Solana dApps")

            val entities = solanaApps.toEntities()
            discoverDao.insertDApps(entities)

            Log.i(TAG, "âœ… dApps refresh completed")
            LoadingState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to refresh dApps", e)
            LoadingState.Error(e, "Failed to refresh dApps: ${e.message}")
        }
    }

    private suspend fun isDAppsStale(): Boolean {
        val lastUpdate = discoverDao.getDAppsLastUpdateTime() ?: return true
        val age = System.currentTimeMillis() - lastUpdate
        val isStale = age > 5.minutes.inWholeMilliseconds

        Log.d(TAG, "â° dApps age check: isStale=$isStale")
        return isStale
    }

    override suspend fun refreshPerps(): LoadingState<Unit> {
        Log.i(TAG, "🔄 refreshPerps() called")

        // Check network connectivity
        val isConnected = networkMonitor.isConnected.value
        Log.d(TAG, "🌐 Network connected: $isConnected")

        if (!isConnected) {
            Log.w(TAG, "⚠️ No internet connection, cannot refresh perps")
            return LoadingState.Error(
                Exception("Offline"),
                "No internet connection. Showing cached data."
            )
        }

        return try {
            Log.d(TAG, "📡 Fetching perps from Drift API...")
            Log.d(TAG, "📡 API endpoint: https://data.api.drift.trade/contracts")

            // ✅ CALL REAL API (not empty list!)
            val response = driftApi.getContracts()

            Log.i(TAG, "✅ Drift API returned ${response.contracts.size} contracts")

            // Filter for PERP products only (API returns SPOT too)
            val perpContracts = response.contracts.filter { it.isPerpetual }
            Log.d(TAG, "✅ Filtered to ${perpContracts.size} PERP contracts (excluded SPOT)")

            if (perpContracts.isEmpty()) {
                Log.w(TAG, "⚠️ No PERP contracts found in API response!")
            } else {
                Log.d(TAG, "📋 First 3 perps: ${perpContracts.take(3).map { it.tickerId }}")
            }

            // Convert DTO → Entity
            Log.d(TAG, "🔄 Converting DTOs to entities...")
            val entities = perpContracts.map { it.toEntity() }
            Log.d(TAG, "✅ Converted to ${entities.size} entities")

            // Save to database
            Log.d(TAG, "💾 Inserting ${entities.size} perps into database...")
            discoverDao.insertPerps(entities)
            Log.i(TAG, "✅ Successfully inserted perps into database")

            // Verify insertion
            val lastUpdate = discoverDao.getPerpsLastUpdateTime()
            Log.d(TAG, "⏰ Last update timestamp: $lastUpdate")

            LoadingState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to refresh perps", e)
            Log.e(TAG, "❌ Error type: ${e.javaClass.simpleName}")
            Log.e(TAG, "❌ Error message: ${e.message}")

            LoadingState.Error(e, "Failed to refresh perps: ${e.message}")
        }
    }
}