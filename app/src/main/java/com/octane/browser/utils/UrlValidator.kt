package com.octane.browser.utils

import java.net.URI

object UrlValidator {
    fun isValidUrl(url: String): Boolean {
        return try {
            val uri = URI(url)
            uri.scheme != null && uri.host != null
        } catch (e: Exception) {
            false
        }
    }

    fun normalizeUrl(input: String): String {
        return when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.contains(".") && !input.contains(" ") -> "https://$input"
            else -> "https://www.google.com/search?q=${java.net.URLEncoder.encode(input, "UTF-8")}"
        }
    }
}