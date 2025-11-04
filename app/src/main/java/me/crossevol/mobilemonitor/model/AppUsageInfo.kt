package me.crossevol.mobilemonitor.model

import android.graphics.drawable.Drawable

/**
 * Data class representing usage information for a single application
 * 
 * @param packageName The unique package identifier for the application
 * @param appName The display name of the application
 * @param icon The application icon drawable, null if not available
 * @param lastTimeUsed Timestamp in milliseconds when the app was last used
 * @param totalTimeInForeground Total time in milliseconds the app was in foreground
 */
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val lastTimeUsed: Long,
    val totalTimeInForeground: Long
)