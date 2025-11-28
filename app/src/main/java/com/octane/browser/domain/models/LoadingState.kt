package com.octane.browser.domain.models

sealed class LoadingState<out T> {
    object Idle : LoadingState<Nothing>()
    object Loading : LoadingState<Nothing>()
    data class Success<T>(val data: T) : LoadingState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : LoadingState<Nothing>()
}