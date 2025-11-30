package com.octane.browser.data.local.db.entity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.ByteArrayOutputStream

/**
 * ✅ ENHANCED: Store complete WebView state
 */
@Entity(tableName = "browser_tabs")
data class BrowserTabEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val timestamp: Long,
    val isActive: Boolean = false,
    val faviconBytes: ByteArray? = null,
    val screenshotBytes: ByteArray? = null,

    // ✅ NEW: WebView state fields
    val scrollX: Int = 0,
    val scrollY: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val progress: Int = 0,
    val isLoading: Boolean = false,
    val isSecure: Boolean = false
) {
    companion object {
        fun bitmapToByteArray(bitmap: Bitmap?): ByteArray? {
            if (bitmap == null) return null
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            return stream.toByteArray()
        }

        fun byteArrayToBitmap(bytes: ByteArray?): Bitmap? {
            if (bytes == null) return null
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BrowserTabEntity

        if (id != other.id) return false
        if (url != other.url) return false
        if (title != other.title) return false
        if (timestamp != other.timestamp) return false
        if (isActive != other.isActive) return false
        if (faviconBytes != null) {
            if (other.faviconBytes == null) return false
            if (!faviconBytes.contentEquals(other.faviconBytes)) return false
        } else if (other.faviconBytes != null) return false
        if (screenshotBytes != null) {
            if (other.screenshotBytes == null) return false
            if (!screenshotBytes.contentEquals(other.screenshotBytes)) return false
        } else if (other.screenshotBytes != null) return false
        if (scrollX != other.scrollX) return false
        if (scrollY != other.scrollY) return false
        if (canGoBack != other.canGoBack) return false
        if (canGoForward != other.canGoForward) return false
        if (progress != other.progress) return false
        if (isLoading != other.isLoading) return false
        if (isSecure != other.isSecure) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + isActive.hashCode()
        result = 31 * result + (faviconBytes?.contentHashCode() ?: 0)
        result = 31 * result + (screenshotBytes?.contentHashCode() ?: 0)
        result = 31 * result + scrollX
        result = 31 * result + scrollY
        result = 31 * result + canGoBack.hashCode()
        result = 31 * result + canGoForward.hashCode()
        result = 31 * result + progress
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + isSecure.hashCode()
        return result
    }
}