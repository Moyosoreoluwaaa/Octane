package com.octane.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.octane.data.local.datastore.UserPreferencesStore
import com.octane.data.local.datastore.UserPreferencesStoreImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin Module for UserPreferencesStore setup
 */

val preferencesModule = module {
    // 1. Provide the DataStore instance
    /**
     * Provides Android DataStore<Preferences> instance.
     * Used for storing user preferences (currency, privacy mode, etc.)
     */
    single {
        androidContext().dataStore
    }

    // 2. Bind the implementation to the interface for DI
    /**
     * Provides UserPreferencesStore implementation.
     * Wraps DataStore with domain-friendly API.
     *
     */
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