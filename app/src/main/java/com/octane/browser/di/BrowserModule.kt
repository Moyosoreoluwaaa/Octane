package com.octane.browser.di

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Room
import com.octane.browser.data.local.datastore.SettingsDataStore
import com.octane.browser.data.local.datastore.SettingsDataStoreImpl
import com.octane.browser.data.local.db.BrowserDatabase
import com.octane.browser.data.repository.*
import com.octane.browser.domain.managers.ThemeManager
import com.octane.browser.domain.repository.*
import com.octane.browser.domain.usecases.bookmark.*
import com.octane.browser.domain.usecases.connection.*
import com.octane.browser.domain.usecases.history.*
import com.octane.browser.domain.usecases.navigation.NavigateToUrlUseCase
import com.octane.browser.domain.usecases.security.*
import com.octane.browser.domain.usecases.settings.*
import com.octane.browser.domain.usecases.tab.*
import com.octane.browser.domain.usecases.validation.ValidateUrlUseCase
import com.octane.browser.presentation.viewmodels.*
import com.octane.browser.wallet_integration.MockWalletConnector
import com.octane.browser.wallet_integration.WalletConnector
import com.octane.browser.webview.AdvancedFeatureManager
import com.octane.browser.webview.WebViewManager
import com.octane.browser.webview.bridge.BridgeManager
import com.octane.browser.webview.bridge.WalletBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import timber.log.Timber

@RequiresApi(Build.VERSION_CODES.O)
val browserModule = module {

    // ========================================
    // CORE - Coroutine Scope
    // ========================================

    single {
        Timber.d("Initializing Application CoroutineScope...")
        CoroutineScope(SupervisorJob()).also {
            Timber.d("✅ Application CoroutineScope initialized")
        }
    }

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
    // DATASTORE - Settings Persistence
    // ========================================

    single<SettingsDataStore> {
        Timber.d("Initializing SettingsDataStore...")
        SettingsDataStoreImpl(androidContext()).also {
            Timber.d("✅ SettingsDataStore initialized")
        }
    }


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

    single<SettingsRepository> {
        Timber.d("Initializing SettingsRepository...")
        SettingsRepositoryImpl(get()).also {
            Timber.d("✅ SettingsRepository initialized")
        }
    }


    // ========================================
    // MANAGERS - Theme & App-level State
    // ========================================

    single {
        Timber.d("Initializing ThemeManager...")
        ThemeManager(
            settingsRepository = get(),
            scope = get()
        ).also {
            Timber.d("✅ ThemeManager initialized")
        }
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
    // USE CASES - Settings
    // ========================================

    factory { ObserveSettingsUseCase(get()) }
    factory { UpdateSettingsUseCase(get()) }
    factory { UpdateThemeUseCase(get()) }
    factory { UpdateDynamicColorsUseCase(get()) }


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

    // SettingsViewModel - New instance per screen (NOW WITH USECASES)
    viewModel {
        SettingsViewModel(
            observeSettingsUseCase = get(),
            updateSettingsUseCase = get(),
            updateThemeUseCase = get(),
            updateDynamicColorsUseCase = get()
        )
    }
}
