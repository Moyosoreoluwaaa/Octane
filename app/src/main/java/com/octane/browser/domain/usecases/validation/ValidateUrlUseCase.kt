package com.octane.browser.domain.usecases.validation

/**
 * ✅ SIMPLIFIED: Minimal validation - let search engine handle everything
 *
 * Rules:
 * 1. If it starts with http:// or https:// → Use as-is (direct URL)
 * 2. Everything else → Send to Google search
 *
 * This way:
 * - "Rema" → Google search for "Rema"
 * - "bold" → Google search for "bold"
 * - "elastic" → Google search for "elastic"
 * - "github.com" → Google search for "github.com" (user can click result)
 * - "https://github.com" → Direct navigation
 */
class ValidateUrlUseCase {

    operator fun invoke(input: String): UrlValidationResult {
        val trimmed = input.trim()

        if (trimmed.isEmpty()) {
            return UrlValidationResult.Empty
        }

        // ✅ ONLY check for explicit protocols - nothing else
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return UrlValidationResult.ValidUrl(trimmed)
        }

        // ✅ Everything else is a search query - let Google figure it out
        return UrlValidationResult.SearchQuery(trimmed)
    }

    sealed class UrlValidationResult {
        data object Empty : UrlValidationResult()

        data class ValidUrl(val url: String) : UrlValidationResult()

        data class SearchQuery(val query: String) : UrlValidationResult() {
            /**
             * ✅ Simple Google search - no fancy logic
             */
            fun toSearchUrl(): String {
                val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                return "https://www.google.com/search?q=$encodedQuery"
            }
        }
    }
}