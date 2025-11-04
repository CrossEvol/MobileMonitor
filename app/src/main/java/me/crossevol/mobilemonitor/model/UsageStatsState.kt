package me.crossevol.mobilemonitor.model

/**
 * Sealed class representing different states of usage statistics data loading
 */
sealed class UsageStatsState {
    /**
     * State indicating that usage statistics are currently being loaded
     */
    object Loading : UsageStatsState()
    
    /**
     * State indicating successful loading of usage statistics
     * 
     * @param apps List of applications with their usage information
     */
    data class Success(val apps: List<AppUsageInfo>) : UsageStatsState()
    
    /**
     * State indicating an error occurred while loading usage statistics
     * 
     * @param message Error message describing what went wrong
     */
    data class Error(val message: String) : UsageStatsState()
    
    /**
     * State indicating that usage access permission is required
     */
    object PermissionRequired : UsageStatsState()
}