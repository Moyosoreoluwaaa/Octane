import android.graphics.Bitmap
import android.webkit.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

/**
 * ✅ NEW: Captures WebView screenshot for tab preview
 */
class CaptureTabScreenshotUseCase {
    suspend operator fun invoke(webView: WebView): Bitmap? = withContext(Dispatchers.Main) {
        try {
            // Create bitmap with WebView dimensions
            val bitmap = createBitmap(webView.width, webView.height)

            // Draw WebView content to bitmap
            val canvas = android.graphics.Canvas(bitmap)
            webView.draw(canvas)

            // Scale down to thumbnail size (save storage)
            val thumbnail = bitmap.scale(400, (400 * bitmap.height) / bitmap.width)

            bitmap.recycle() // Release original

            Timber.d("📸 Captured tab screenshot: ${thumbnail.width}x${thumbnail.height}")
            thumbnail

        } catch (e: Exception) {
            Timber.e(e, "Failed to capture screenshot")
            null
        }
    }
}