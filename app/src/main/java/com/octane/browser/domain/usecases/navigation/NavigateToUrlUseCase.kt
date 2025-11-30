package com.octane.browser.domain.usecases.navigation

import com.octane.browser.domain.usecases.history.RecordVisitUseCase
import com.octane.browser.domain.usecases.tab.UpdateTabContentUseCase
import java.net.URLEncoder

/**
 * ✅ FIXED: Smart URL detection + Google search fallback
 *
 * Logic:
 * 1. Has "http://" or "https://" → Load as URL
 * 2. Contains "." and looks like domain → Add "https://" and load
 * 3. Everything else → Search Google
 */
class NavigateToUrlUseCase(
    private val updateTabContentUseCase: UpdateTabContentUseCase,
    private val recordVisitUseCase: RecordVisitUseCase
) {
    suspend operator fun invoke(
        tabId: String,
        input: String,
        currentTitle: String = ""
    ): NavigationResult {
        val trimmed = input.trim()

        if (trimmed.isEmpty()) {
            return NavigationResult.Error("Empty input")
        }

        val finalUrl = detectUrlOrSearch(trimmed)

        updateTabContentUseCase(
            tabId = tabId,
            url = finalUrl,
            title = currentTitle.ifEmpty { trimmed },
            favicon = null
        )

        recordVisitUseCase(finalUrl, currentTitle.ifEmpty { trimmed })

        return NavigationResult.Success(finalUrl)
    }

    /**
     * ✅ DEAD SIMPLE: URL vs Search
     *
     * URL IF:
     * - Has "http://" OR "https://"
     * - Has "www."
     * - Has "." (like google.com)
     *
     * ELSE: Google Search
     */
    private fun detectUrlOrSearch(input: String): String {
        // 1. Has http:// or https:// → Use as-is
        if (input.startsWith("http://") || input.startsWith("https://")) {
            return input
        }

        // 2. Has www. → Add https://
        if (input.startsWith("www.")) {
            return "https://$input"
        }

        // 3. Has a dot (.) → Assume domain, add https://
        if (input.contains('.')) {
            return "https://$input"
        }

        // 4. Everything else → Google search
        val encoded = URLEncoder.encode(input, "UTF-8")
        return "https://www.google.com/search?q=$encoded"
    }

    sealed class NavigationResult {
        data class Success(val url: String) : NavigationResult()
        data class Error(val message: String) : NavigationResult()
    }
}

/**
 * ✅ SIMPLE Test Cases:
 *
 * URLs (has "." OR "http://" OR "www"):
 * "google.com"          → https://google.com
 * "github.com"          → https://github.com
 * "192.168.1.1"         → https://192.168.1.1
 * "www.example.com"     → https://www.example.com
 * "http://example.com"  → http://example.com
 * "https://github.com"  → https://github.com
 *
 * Google Search (no "." AND no "http://" AND no "www"):
 * "bold"                → https://www.google.com/search?q=bold
 * "how to cook"         → https://www.google.com/search?q=how+to+cook
 * "rema"                → https://www.google.com/search?q=rema
 * "hello world"         → https://www.google.com/search?q=hello+world
 * "what is bitcoin"     → https://www.google.com/search?q=what+is+bitcoin
 */