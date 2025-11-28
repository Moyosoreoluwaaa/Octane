package com.octane.browser.domain.usecases.security

class ValidateSslUseCase {
    operator fun invoke(url: String): Boolean {
        return url.startsWith("https://")
    }
}