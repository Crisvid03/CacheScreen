import android.graphics.drawable.Drawable

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usageTimeMillis: Long,
    val appIcon: Drawable?
)
