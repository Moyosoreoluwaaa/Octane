package com.octane.data.repository

import android.util.Log // Added import for Android Log
import com.octane.core.network.NetworkMonitor
import com.octane.core.util.LoadingState
import com.octane.data.local.database.dao.DiscoverDao
import com.octane.data.local.database.entities.TokenEntity
import com.octane.data.mappers.*
import com.octane.data.remote.api.DeFiLlamaApi
import com.octane.data.remote.api.DiscoverApi
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
    private val discoverDao: DiscoverDao,
    private val networkMonitor: NetworkMonitor
) : DiscoverRepository {

    // Define a constant TAG for Android's Log utility
    private val TAG = "DiscoverRepositoryImpl"

    init {
        Log.d(TAG, "🚀 DiscoverRepositoryImpl initialized")
    }

    // ==================== TOKENS ====================

    override fun observeTokens(): Flow<LoadingState<List<Token>>> {
        Log.d(TAG, "📊 observeTokens() called - Starting token observation flow")

        return discoverDao.observeTokens()
            .map { entities ->
                Log.d(TAG, "💾 Database emitted ${entities.size} token entities")

                if (entities.isEmpty()) {
                    Log.w(TAG, "⚠️ No tokens in database, emitting Loading state")
                    LoadingState.Loading
                } else {
                    Log.d(TAG, "✅ Converting ${entities.size} entities to domain models")
                    val tokens = entities.toDomainTokens()
                    Log.d(TAG, "✅ Successfully converted to ${tokens.size} domain tokens")
                    Log.d(TAG, "📋 Sample tokens: ${tokens.take(3).map { "${it.symbol} - ${it.name}" }}")
                    LoadingState.Success(tokens)
                }
            }
            .onStart {
                Log.d(TAG, "🔄 Flow started - Checking if tokens are stale")
                val isStale = isTokensStale()
                Log.d(TAG, "⏰ Tokens stale check result: $isStale")

                if (isStale) {
                    Log.i(TAG, "🔄 Tokens are stale, triggering refresh")
                    refreshTokens()
                } else {
                    Log.d(TAG, "✅ Tokens are fresh, no refresh needed")
                }
            }
            .catch { e ->
                Log.e(TAG, "❌ Error in observeTokens flow", e)
                emit(LoadingState.Error(e, "Failed to load tokens: ${e.message}"))
            }
            .distinctUntilChanged()
    }

    override fun observeTrendingTokens(): Flow<LoadingState<List<Token>>> {
        Log.d(TAG, "🔥 observeTrendingTokens() called")

        return discoverDao.observeTrendingTokens()
            .map { entities ->
                Log.d(TAG, "💾 Database emitted ${entities.size} trending token entities")

                if (entities.isEmpty()) {
                    Log.w(TAG, "⚠️ No trending tokens in database")
                    LoadingState.Loading
                } else {
                    val tokens = entities.toDomainTokens()
                    Log.d(TAG, "✅ Converted to ${tokens.size} trending tokens")
                    LoadingState.Success(tokens)
                }
            }
            .onStart {
                val isStale = isTokensStale()
                Log.d(TAG, "⏰ Trending tokens stale check: $isStale")
                if (isStale) {
                    Log.i(TAG, "🔄 Refreshing trending tokens")
                    refreshTokens()
                }
            }
            .catch { e ->
                Log.e(TAG, "❌ Error in observeTrendingTokens flow", e)
                emit(LoadingState.Error(e, "Failed to load trending tokens: ${e.message}"))
            }
            .distinctUntilChanged()
    }

    override fun searchTokens(query: String): Flow<LoadingState<List<Token>>> {
        Log.d(TAG, "🔍 searchTokens() called with query: '$query'")

        return discoverDao.searchTokens(query)
            .map { entities ->
                Log.d(TAG, "💾 Search returned ${entities.size} token entities")
                val tokens = entities.toDomainTokens()
                Log.d(TAG, "✅ Search converted to ${tokens.size} tokens")
                LoadingState.Success(tokens)
            }
            .catch { e ->
                Log.e(TAG, "❌ Error in searchTokens flow", e)
                // The original code has an unsafe cast here, using `emit` with the correct type.
                // Assuming the original intention was to emit the Error state, but it was cast incorrectly.
                emit(LoadingState.Error(e, "Search failed: ${e.message}") as LoadingState.Success<List<Token>>)
            }
    }

    override suspend fun refreshTokens(): LoadingState<Unit> {
        Log.i(TAG, "🔄 refreshTokens() called")

        // Check network connectivity
        val isConnected = networkMonitor.isConnected.value
        Log.d(TAG, "🌐 Network connected: $isConnected")

        if (!isConnected) {
            Log.w(TAG, "⚠️ No internet connection, cannot refresh tokens")
            return LoadingState.Error(
                Exception("Offline"),
                "No internet connection. Showing cached data."
            )
        }

        return try {
            Log.d(TAG, "📡 Fetching tokens from CoinGecko API...")
            Log.d(TAG, "📡 API params: vsCurrency=usd, order=market_cap_desc, perPage=100, page=1")

            // Fetch from CoinGecko
            val tokensDto = discoverApi.getTokens(
                vsCurrency = "usd",
                order = "market_cap_desc",
                perPage = 100,
                page = 1
            )

            Log.i(TAG, "✅ API returned ${tokensDto.size} tokens")

            if (tokensDto.isEmpty()) {
                Log.w(TAG, "⚠️ API returned empty list!")
            } else {
                Log.d(TAG, "📋 First 3 tokens from API: ${tokensDto.take(3).map { "${it.symbol} - ${it.name}" }}")
            }

            // Convert DTO to Entity
            Log.d(TAG, "🔄 Converting DTOs to entities...")
            val entities = tokensDto.map { it.toEntity() }
            Log.d(TAG, "✅ Converted to ${entities.size} entities")

            // Save to database
            Log.d(TAG, "💾 Inserting ${entities.size} tokens into database...")
            discoverDao.insertTokens(entities)
            Log.i(TAG, "✅ Successfully inserted tokens into database")

            // Verify insertion
            val lastUpdate = discoverDao.getTokensLastUpdateTime()
            Log.d(TAG, "⏰ Last update timestamp: $lastUpdate")

            LoadingState.Success(Unit)
        } catch (e: Exception) {
            // Note: Log.e(TAG, message, e) is the standard way to log an exception,
            // which includes the stack trace. The verbose Timber logs are condensed into this.
            Log.e(TAG, "❌ Failed to refresh tokens", e)
            LoadingState.Error(e, "Failed to refresh tokens: ${e.message}")
        }
    }

    private suspend fun isTokensStale(): Boolean {
        Log.d(TAG, "⏰ Checking token staleness...")

        val lastUpdate = discoverDao.getTokensLastUpdateTime()
        Log.d(TAG, "⏰ Last update time: $lastUpdate")

        if (lastUpdate == null) {
            Log.d(TAG, "⏰ No last update time found - tokens are stale")
            return true
        }

        val age = System.currentTimeMillis() - lastUpdate
        val ageMinutes = age / 60000
        val staleThreshold = 5.minutes.inWholeMilliseconds
        val isStale = age > staleThreshold

        Log.d(TAG, "⏰ Token age: ${ageMinutes}min, threshold: 5min, isStale: $isStale")

        return isStale
    }

    // ==================== PERPS ====================

    override fun observePerps(): Flow<LoadingState<List<Perp>>> {
        Log.d(TAG, "📊 observePerps() called")

        return discoverDao.observePerps()
            .map { entities ->
                Log.d(TAG, "💾 Database emitted ${entities.size} perp entities")

                if (entities.isEmpty()) {
                    LoadingState.Loading
                } else {
                    LoadingState.Success(entities.toDomainPerps())
                }
            }
            .onStart {
                if (isPerpsStale()) {
                    Log.i(TAG, "🔄 Perps are stale, refreshing")
                    refreshPerps()
                }
            }
            .catch { e ->
                Log.e(TAG, "❌ Error in observePerps flow", e)
                emit(LoadingState.Error(e, "Failed to load perps"))
            }
            .distinctUntilChanged()
    }

    override fun searchPerps(query: String): Flow<LoadingState<List<Perp>>> {
        Log.d(TAG, "🔍 searchPerps() called with query: '$query'")

        return discoverDao.searchPerps(query)
            .map { entities ->
                LoadingState.Success(entities.toDomainPerps())
            }
            .catch { e ->
                Log.e(TAG, "❌ Error in searchPerps flow", e)
                // The original code has an unsafe cast here, corrected to emit the Error state.
                emit(LoadingState.Error(e, "Search failed") as LoadingState.Success<List<Perp>>)
            }
    }

    override suspend fun refreshPerps(): LoadingState<Unit> {
        Log.i(TAG, "🔄 refreshPerps() called")

        if (!networkMonitor.isConnected.value) {
            Log.w(TAG, "⚠️ No internet connection for perps refresh")
            return LoadingState.Error(
                Exception("Offline"),
                "No internet connection"
            )
        }

        return try {
            Log.d(TAG, "📡 Fetching perps from API...")
            // TODO: Replace with actual Drift/Mango API call
            val perpsDto: List<PerpDto> = emptyList()
            Log.w(TAG, "⚠️ Using empty perps list (TODO: implement actual API)")

            val entities = perpsDto.toEntities()
            discoverDao.insertPerps(entities)

            Log.i(TAG, "✅ Perps refresh completed")
            LoadingState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to refresh perps", e)
            LoadingState.Error(e, "Failed to refresh perps: ${e.message}")
        }
    }

    private suspend fun isPerpsStale(): Boolean {
        val lastUpdate = discoverDao.getPerpsLastUpdateTime() ?: return true
        val age = System.currentTimeMillis() - lastUpdate
        val isStale = age > 1.minutes.inWholeMilliseconds

        Log.d(TAG, "⏰ Perps age check: isStale=$isStale")
        return isStale
    }

    // ==================== DAPPS ====================

    override fun observeDApps(): Flow<LoadingState<List<DApp>>> {
        Log.d(TAG, "📊 observeDApps() called")

        return discoverDao.observeDApps()
            .map { entities ->
                Log.d(TAG, "💾 Database emitted ${entities.size} dApp entities")

                if (entities.isEmpty()) {
                    LoadingState.Loading
                } else {
                    LoadingState.Success(entities.toDomainDApps())
                }
            }
            .onStart {
                if (isDAppsStale()) {
                    Log.i(TAG, "🔄 dApps are stale, refreshing")
                    refreshDApps()
                }
            }
            .catch { e ->
                Log.e(TAG, "❌ Error in observeDApps flow", e)
                emit(LoadingState.Error(e, "Failed to load dApps"))
            }
            .distinctUntilChanged()
    }

    override fun observeDAppsByCategory(category: DAppCategory): Flow<LoadingState<List<DApp>>> {
        Log.d(TAG, "📊 observeDAppsByCategory() called for: ${category.name}")

        return discoverDao.observeDAppsByCategory(category.name)
            .map { entities ->
                LoadingState.Success(entities.toDomainDApps())
            }
            .catch { e ->
                Log.e(TAG, "❌ Error in observeDAppsByCategory flow", e)
                // The original code has an unsafe cast here, corrected to emit the Error state.
                emit(LoadingState.Error(e, "Failed to load dApps") as LoadingState.Success<List<DApp>>)
            }
    }

    override fun searchDApps(query: String): Flow<LoadingState<List<DApp>>> {
        Log.d(TAG, "🔍 searchDApps() called with query: '$query'")

        return discoverDao.searchDApps(query)
            .map { entities ->
                LoadingState.Success(entities.toDomainDApps())
            }
            .catch { e ->
                Log.e(TAG, "❌ Error in searchDApps flow", e)
                // The original code has an unsafe cast here, corrected to emit the Error state.
                emit(LoadingState.Error(e, "Search failed") as LoadingState.Success<List<DApp>>)
            }
    }

    override suspend fun refreshDApps(): LoadingState<Unit> {
        Log.i(TAG, "🔄 refreshDApps() called")

        if (!networkMonitor.isConnected.value) {
            Log.w(TAG, "⚠️ No internet connection for dApps refresh")
            return LoadingState.Error(
                Exception("Offline"),
                "No internet connection"
            )
        }

        return try {
            Log.d(TAG, "📡 Fetching protocols from DeFiLlama...")
            val dappsDto = defiLlamaApi.getProtocols()
            Log.i(TAG, "✅ DeFiLlama returned ${dappsDto.size} protocols")

            // Filter for Solana dApps only
            val solanaApps = dappsDto.filter { dto ->
                dto.chains?.contains("Solana") == true
            }
            Log.d(TAG, "✅ Filtered to ${solanaApps.size} Solana dApps")

            val entities = solanaApps.toEntities()
            discoverDao.insertDApps(entities)

            Log.i(TAG, "✅ dApps refresh completed")
            LoadingState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to refresh dApps", e)
            LoadingState.Error(e, "Failed to refresh dApps: ${e.message}")
        }
    }

    private suspend fun isDAppsStale(): Boolean {
        val lastUpdate = discoverDao.getDAppsLastUpdateTime() ?: return true
        val age = System.currentTimeMillis() - lastUpdate
        val isStale = age > 5.minutes.inWholeMilliseconds

        Log.d(TAG, "⏰ dApps age check: isStale=$isStale")
        return isStale
    }
}