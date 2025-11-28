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
import com.octane.browser.webview.WebViewManager
import com.octane.browser.webview.bridge.BridgeManager
import com.octane.browser.webview.bridge.WalletBridge
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val browserModule = module {

    // ========================================
    // DATABASE & DAOs
    // ========================================

    single {
        Room.databaseBuilder(
            androidContext(),
            BrowserDatabase::class.java,
            "octane_browser.db"
        )
            .fallbackToDestructiveMigration() // For development
            .build()
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
    // WEBVIEW COMPONENTS
    // ========================================

    // WalletBridge (scoped to manage state)
    single {
        WalletBridge()
    }

    // BridgeManager (singleton - manages JS injection)
    single {
        BridgeManager(get())
    }

    // WebViewManager (singleton - creates WebViews)
    single {
        WebViewManager(
            context = androidContext(),
            bridgeManager = get()
        )
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
 * 1. Navigation Setup:
 *    - BrowserDestinations.kt: Type-safe routes
 *    - BrowserNavGraph.kt: Complete navigation graph
 *    - BrowserApp.kt: Entry point composable
 *
 * 2. Bottom Sheets (Replace Dialogs):
 *    - AddBookmarkBottomSheet: Save bookmarks
 *    - ConfirmationBottomSheet: Generic confirmation
 *    - PhishingWarningBottomSheet: Security warning
 *    - ConnectionRequestBottomSheet: Web3 connection approval
 *    - ClearDataBottomSheet: Clear browsing data with options
 *
 * 3. Manifest Updates:
 *    - Hardware acceleration enabled
 *    - Deep linking for WalletConnect (wc://)
 *    - Storage permissions for downloads
 *    - WebView optimizations
 *
 * 4. Koin DI Module:
 *    - Complete browserModule with all dependencies
 *    - Database, DAOs, Repositories
 *    - All Use Cases (18 total)
 *    - WebView components (Manager, Bridge, Wallet)
 *    - All ViewModels (6 total)
 *    - Integrated into OctaneApplication
 *
 * Testing Setup:
 *    - MainActivity now shows BrowserApp() for testing
 *    - After testing, uncomment wallet navigation
 *    - All dependencies properly injected via Koin
 */