import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.octane.browser.data.local.db.BitmapConverter

//import com.octane.browser.data.local.db.BitmapConverter

/**
 * ✅ Quick Access Entity with Favicon Storage
 */
@Entity(tableName = "quick_access")
@TypeConverters(BitmapConverter::class)
data class QuickAccessEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val title: String,
    val favicon: Bitmap? = null,
    val position: Int = 0, // For ordering
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)