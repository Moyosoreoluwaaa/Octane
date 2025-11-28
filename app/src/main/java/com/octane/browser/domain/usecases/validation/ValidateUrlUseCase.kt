package com.octane.browser.domain.usecases.validation

import android.util.Patterns
import java.net.URI

class ValidateUrlUseCase {
    operator fun invoke(input: String): UrlValidationResult {
        val trimmed = input.trim()

        return when {
            trimmed.isEmpty() -> UrlValidationResult.Empty

            // Already a valid URL
            isValidUrl(trimmed) -> UrlValidationResult.ValidUrl(trimmed)

            // Domain-like (contains dot, no spaces)
            isDomainLike(trimmed) -> {
                val withProtocol = "https://$trimmed"
                UrlValidationResult.ValidUrl(withProtocol)
            }

            // Looks like a search query
            else -> UrlValidationResult.SearchQuery(trimmed)
        }
    }

    private fun isValidUrl(input: String): Boolean {
        return try {
            val uri = URI(input)
            uri.scheme != null && uri.host != null &&
                    (uri.scheme == "http" || uri.scheme == "https")
        } catch (e: Exception) {
            false
        }
    }

    private fun isDomainLike(input: String): Boolean {
        return input.contains(".") &&
                !input.contains(" ") &&
                Patterns.WEB_URL.matcher("https://$input").matches()
    }

    sealed class UrlValidationResult {
        object Empty : UrlValidationResult()
        data class ValidUrl(val url: String) : UrlValidationResult()
        data class SearchQuery(val query: String) : UrlValidationResult() {
            fun toSearchUrl(searchEngine: String = "https://www.google.com/search?q="): String {
                return "$searchEngine${java.net.URLEncoder.encode(query, "UTF-8")}"
            }
        }
    }
}
