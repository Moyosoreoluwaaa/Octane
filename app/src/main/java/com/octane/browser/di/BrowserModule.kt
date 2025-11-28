package com.octane.browser.di

import androidx.room.Room
import com.octane.browser.data.local.db.BrowserDatabase
import com.octane.browser.data.repository.*
import com.octane.browser.domain.repository.*
import com.octane.browser.domain.usecases.bookmark.*
import com.octane.browser.domain.usecases.connection.*
import com.octane.browser.domain.usecases.history.*
import com.octane.browser.domain.usecases.navigation.NavigateToUrlUseCase
import com.octane.browser.domain.usecases.security.*
import com.octane.browser.domain.usecases.tab.*
import com.octane.browser.domain.usecases.validation.ValidateUrlUseCase
import com.octane.browser.presentation.viewmodels.*
import com.octane.browser.wallet_integration.MockWalletConnector
import com.octane.browser.wallet_integration.WalletConnector
import com.octane.browser.webview.AdvancedFeatureManager
import com.octane.browser.webview.WebViewManager
import com.octane.browser.webview.bridge.BridgeManager
import com.octane.browser.webview.bridge.WalletBridge
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import timber.log.Timber

val browserModule = module {

    // ========================================
    // DATABASE & DAOs
    // ========================================

    single {
        Timber.d("Initializing Browser Database...")
        Room.databaseBuilder(
            androidContext(),
            BrowserDatabase::class.java,
            "octane_browser.db"
        )
            .fallbackToDestructiveMigration() // For development
            .build()
            .also { Timber.d("✅ Database initialized") }
    }

    single { get<BrowserDatabase>().tabDao() }
    single { get<BrowserDatabase>().bookmarkDao() }
    single { get<BrowserDatabase>().historyDao() }
    single { get<BrowserDatabase>().connectionDao() }


    // ========================================
    // REPOSITORIES
    // ========================================

    single<TabRepository> {
        TabRepositoryImpl(get())
    }

    single<BookmarkRepository> {
        BookmarkRepositoryImpl(get())
    }

    single<HistoryRepository> {
        HistoryRepositoryImpl(get())
    }

    single<ConnectionRepository> {
        ConnectionRepositoryImpl(get())
    }


    // ========================================
    // USE CASES - Tab Management
    // ========================================

    factory { CreateNewTabUseCase(get()) }
    factory { CloseTabUseCase(get()) }
    factory { SwitchTabUseCase(get()) }
    factory { UpdateTabContentUseCase(get()) }
    factory { GetActiveTabUseCase(get()) }


    // ========================================
    // USE CASES - Bookmarks
    // ========================================

    factory { AddBookmarkUseCase(get()) }
    factory { RemoveBookmarkUseCase(get()) }
    factory { ToggleBookmarkUseCase(get()) }


    // ========================================
    // USE CASES - History
    // ========================================

    factory { RecordVisitUseCase(get()) }
    factory { SearchHistoryUseCase(get()) }
    factory { ClearHistoryUseCase(get()) }


    // ========================================
    // USE CASES - Connections
    // ========================================

    factory { RequestConnectionUseCase(get()) }
    factory { DisconnectDAppUseCase(get()) }
    factory { GetConnectionUseCase(get()) }


    // ========================================
    // USE CASES - Validation & Security
    // ========================================

    factory { ValidateUrlUseCase() }
    factory { CheckPhishingUseCase() }
    factory { ValidateSslUseCase() }


    // ========================================
    // USE CASES - Navigation
    // ========================================

    factory {
        NavigateToUrlUseCase(
            validateUrlUseCase = get(),
            updateTabContentUseCase = get(),
            recordVisitUseCase = get()
        )
    }


    // ========================================
    // WEBVIEW COMPONENTS (CRITICAL ORDER)
    // ========================================

    // 1. WalletBridge (singleton - manages wallet integration)
    single {
        Timber.d("Initializing WalletBridge...")
        WalletBridge().also { Timber.d("✅ WalletBridge initialized") }
    }

    // 2. BridgeManager (singleton - manages JS injection)
    single {
        Timber.d("Initializing BridgeManager...")
        BridgeManager(get()).also { bridge ->
            // Set bidirectional reference
            get<WalletBridge>().setBridgeManager(bridge)
            Timber.d("✅ BridgeManager initialized")
        }
    }

    // 3. AdvancedFeatureManager (singleton - manages advanced web features)
    single {
        Timber.d("Initializing AdvancedFeatureManager...")
        AdvancedFeatureManager(androidContext()).also {
            Timber.d("✅ AdvancedFeatureManager initialized")
        }
    }

    // 4. WebViewManager (singleton - creates WebViews)
    single {
        Timber.d("Initializing WebViewManager...")
        WebViewManager(
            context = androidContext(),
            bridgeManager = get(),
            featureManager = get()
        ).also {
            Timber.d("✅ WebViewManager initialized")

            // Log WebView availability
            if (WebViewManager.isWebViewAvailable(androidContext())) {
                val version = WebViewManager.getWebViewVersion(androidContext())
                Timber.d("📱 WebView version: $version")
            } else {
                Timber.e("❌ WebView not available!")
            }
        }
    }


    // ========================================
    // WALLET INTEGRATION
    // ========================================

    single<WalletConnector> {
        // TODO: Replace with real WalletConnector when wallet module is ready
        MockWalletConnector()
    }


    // ========================================
    // VIEWMODELS
    // ========================================

    // BrowserViewModel - SHARED across browser screens
    viewModel {
        BrowserViewModel(
            createNewTabUseCase = get(),
            closeTabUseCase = get(),
            switchTabUseCase = get(),
            updateTabContentUseCase = get(),
            getActiveTabUseCase = get(),
            navigateToUrlUseCase = get(),
            toggleBookmarkUseCase = get(),
            recordVisitUseCase = get(),
            checkPhishingUseCase = get(),
            validateSslUseCase = get(),
            bookmarkRepository = get(),
            tabRepository = get()
        )
    }

    // TabManagerViewModel - New instance per screen
    viewModel {
        TabManagerViewModel(
            tabRepository = get(),
            createNewTabUseCase = get(),
            closeTabUseCase = get()
        )
    }

    // BookmarkViewModel - New instance per screen
    viewModel {
        BookmarkViewModel(
            bookmarkRepository = get(),
            addBookmarkUseCase = get(),
            removeBookmarkUseCase = get()
        )
    }

    // HistoryViewModel - New instance per screen
    viewModel {
        HistoryViewModel(
            historyRepository = get(),
            searchHistoryUseCase = get(),
            clearHistoryUseCase = get()
        )
    }

    // ConnectionViewModel - SHARED across browser
    viewModel {
        ConnectionViewModel(
            connectionRepository = get(),
            requestConnectionUseCase = get(),
            disconnectDAppUseCase = get(),
            getConnectionUseCase = get()
        )
    }

    // SettingsViewModel - New instance per screen
    viewModel {
        SettingsViewModel()
    }
}

/**
 * 📝 SETUP CHECKLIST
 *
 * ✅ 1. Database & DAOs configured
 * ✅ 2. Repositories configured
 * ✅ 3. Use Cases configured (18 total)
 * ✅ 4. WebView components configured (proper dependency order)
 * ✅ 5. ViewModels configured (6 total)
 * ✅ 6. Logging enabled for debugging
 *
 * 🔍 DEBUGGING
 *
 * - Check Logcat for "BrowserModule" tags
 * - Verify WebView version is modern (142+)
 * - Confirm no DI errors during startup
 *
 * 🎯 NEXT STEPS
 *
 * 1. Test on complex websites (drift.trade, uniswap, etc.)
 * 2. Monitor GPU errors (should be reduced)
 * 3. Check page load times in logs
 * 4. Verify Service Workers work (PWAs)
 */