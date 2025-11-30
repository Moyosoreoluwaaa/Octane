package com.octane.browser.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URI
import java.net.URL

/**
 * ✅ Utility to fetch favicon from URL
 */
object FaviconFetcher {

    /**
     * Fetch favicon for a given URL
     * Tries multiple sources in order:
     * 1. /favicon.ico
     * 2. Google favicon service
     * 3. DuckDuckGo favicon service
     */
    suspend fun fetchFavicon(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val domain = extractDomain(url) ?: return@withContext null
            
            // Try direct favicon.ico
            val faviconUrl = "https://$domain/favicon.ico"
            fetchFromUrl(faviconUrl)?.let { return@withContext it }

            // Try Google's favicon service
            val googleUrl = "https://www.google.com/s2/favicons?domain=$domain&sz=64"
            fetchFromUrl(googleUrl)?.let { return@withContext it }

            // Try DuckDuckGo's favicon service
            val duckUrl = "https://icons.duckduckgo.com/ip3/$domain.ico"
            fetchFromUrl(duckUrl)?.let { return@withContext it }

            Timber.w("Failed to fetch favicon for: $url")
            null
            
        } catch (e: Exception) {
            Timber.e(e, "Error fetching favicon for: $url")
            null
        }
    }

    /**
     * Fetch bitmap from URL
     */
    private fun fetchFromUrl(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            
            val inputStream = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            // Validate bitmap
            if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
                // Scale to standard size
                scaleBitmap(bitmap, 64)
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.d("Failed to fetch from: $url")
            null
        }
    }

    /**
     * Extract domain from URL
     */
    private fun extractDomain(url: String): String? {
        return try {
            val uri = URI(url)
            uri.host?.removePrefix("www.") ?: url
        } catch (e: Exception) {
            // Try simple parsing
            url.removePrefix("http://")
                .removePrefix("https://")
                .removePrefix("www.")
                .split("/")
                .firstOrNull()
        }
    }

    /**
     * Scale bitmap to target size
     */
    private fun scaleBitmap(bitmap: Bitmap, targetSize: Int): Bitmap {
        return if (bitmap.width != targetSize || bitmap.height != targetSize) {
            Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true).also {
                if (it != bitmap) {
                    bitmap.recycle()
                }
            }
        } else {
            bitmap
        }
    }

    /**
     * Fetch and update favicon for Quick Access
     */
    suspend fun fetchAndUpdateFavicon(
        url: String,
        onUpdate: suspend (Bitmap?) -> Unit
    ) {
        val favicon = fetchFavicon(url)
        onUpdate(favicon)
    }
}