package com.octane.browser.data.local.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

/**
 * Converts Bitmap objects to a storable ByteArray (BLOB) and vice-versa for Room persistence.
 */
class BitmapConverter {

    /**
     * Converts a ByteArray (retrieved from the database) back into a Bitmap.
     * @param bytes The ByteArray representation of the image.
     * @return The resulting Bitmap, or null if the input is null.
     */
    @TypeConverter
    fun toBitmap(bytes: ByteArray?): Bitmap? {
        return bytes?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)
        }
    }

    /**
     * Converts a Bitmap to a ByteArray for storage in the database.
     * @param bitmap The Bitmap to be converted.
     * @return The ByteArray representation of the Bitmap, compressed as PNG.
     */
    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null) return null

        // Use ByteArrayOutputStream to compress the Bitmap into a byte array
        val outputStream = ByteArrayOutputStream()
        // PNG is lossless and generally good for favicons/icons
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}