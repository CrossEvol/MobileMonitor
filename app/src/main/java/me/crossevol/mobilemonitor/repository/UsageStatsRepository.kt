package me.crossevol.mobilemonitor.repository

import me.crossevol.mobilemonitor.model.AppUsageInfo
import me.crossevol.mobilemonitor.model.TimeFilter

/**
 * Repository interface for accessing usage statistics data
 */
interface UsageStatsRepository {
    
    /**
     * Retrieves usage statistics for applications based on the specified time filter
     * 
     * @param timeFilter The time period to filter usage statistics
     * @return Result containing list of app usage information or error
     */
    suspend fun getUsageStats(timeFilter: TimeFilter): Result<List<AppUsageInfo>>
    
    /**
     * Checks if the app has usage access permission
     * 
     * @return true if permission is granted, false otherwise
     */
    fun hasUsageStatsPermission(): Boolean
    
    /**
     * Checks if the app has overlay permission (display over other apps)
     * 
     * @return true if permission is granted, false otherwise
     */
    fun hasOverlayPermission(): Boolean
    
    /**
     * Checks if the app's accessibility service is enabled
     * 
     * @return true if service is enabled, false otherwise
     */
    fun hasAccessibilityPermission(): Boolean
    
    /**
     * Retrieves app metadata (name and icon) for a given package name
     * 
     * @param packageName The package name of the application
     * @return AppUsageInfo with metadata or null if not found
     */
    suspend fun getAppInfo(packageName: String): AppUsageInfo?
}