package com.octane.browser.di

import com.octane.browser.data.repository.QuickAccessRepositoryImpl
import com.octane.browser.domain.repository.QuickAccessRepository
import com.octane.browser.domain.usecases.AddQuickAccessUseCase
import com.octane.browser.domain.usecases.DeleteQuickAccessUseCase
import com.octane.browser.domain.usecases.GetAllQuickAccessUseCase
import com.octane.browser.domain.usecases.GetQuickAccessCountUseCase
import com.octane.browser.domain.usecases.ReorderQuickAccessUseCase
import com.octane.browser.domain.usecases.UpdateQuickAccessFaviconUseCase
import com.octane.browser.domain.usecases.UpdateQuickAccessUseCase
import com.octane.browser.presentation.viewmodels.QuickAccessViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * ✅ Koin Module for Quick Access Feature
 */
val quickAccessModule = module {

    // Repository
    single<QuickAccessRepository> { 
        QuickAccessRepositoryImpl(get()) // get() injects QuickAccessDao
    }

    // Use Cases
    factory { GetAllQuickAccessUseCase(get()) }
    factory { AddQuickAccessUseCase(get()) }
    factory { UpdateQuickAccessUseCase(get()) }
    factory { DeleteQuickAccessUseCase(get()) }
    factory { ReorderQuickAccessUseCase(get()) }
    factory { UpdateQuickAccessFaviconUseCase(get()) }
    factory { GetQuickAccessCountUseCase(get()) }

    // ViewModel
    viewModel { 
        QuickAccessViewModel(
            getAllQuickAccessUseCase = get(),
            addQuickAccessUseCase = get(),
            updateQuickAccessUseCase = get(),
            deleteQuickAccessUseCase = get(),
            reorderQuickAccessUseCase = get(),
            updateQuickAccessFaviconUseCase = get(),
            getQuickAccessCountUseCase = get()
        )
    }
}