package com.octane.browser.data.local.db.entity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.ByteArrayOutputStream
/**
 * ✅ ENHANCED: Bitmap storage using ByteArray
 */
@Entity(tableName = "browser_tabs")
data class BrowserTabEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val timestamp: Long,
    val isActive: Boolean = false,
    val faviconBytes: ByteArray? = null,
    val screenshotBytes: ByteArray? = null // ✅ NEW: Screenshot storage
) {
    // Convert Bitmap to ByteArray for storage
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

        if (timestamp != other.timestamp) return false
        if (isActive != other.isActive) return false
        if (id != other.id) return false
        if (url != other.url) return false
        if (title != other.title) return false
        if (!faviconBytes.contentEquals(other.faviconBytes)) return false
        if (!screenshotBytes.contentEquals(other.screenshotBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + isActive.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (faviconBytes?.contentHashCode() ?: 0)
        result = 31 * result + (screenshotBytes?.contentHashCode() ?: 0)
        return result
    }
}
