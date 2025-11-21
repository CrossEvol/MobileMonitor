package me.crossevol.mobilemonitor.model

import android.graphics.drawable.Drawable

/**
 * Domain model representing application information
 * 
 * @param id Unique identifier for the app
 * @param appName Display name of the application
 * @param packageName Unique package identifier
 * @param enabled Whether monitoring is enabled for this app
 * @param icon Application icon drawable
 * @param lastTimeUsed Timestamp when app was last used
 * @param totalTimeInForeground Total time app was in foreground
 * @param createdTime Timestamp when this record was created
 */
data class AppInfo(
    val id: Long = 0,
    val appName: String,
    val packageName: String,
    val enabled: Boolean = true,
    val icon: Drawable? = null,
    val lastTimeUsed: Long = 0,
    val totalTimeInForeground: Long = 0,
    val createdTime: Long = System.currentTimeMillis()
)
