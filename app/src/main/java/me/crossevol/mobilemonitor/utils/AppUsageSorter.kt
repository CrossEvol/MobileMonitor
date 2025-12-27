package me.crossevol.mobilemonitor.utils

import me.crossevol.mobilemonitor.model.AppUsageInfo

/**
 * Utility object for consistent sorting of app usage data across the application.
 * Implements the sorting requirements specified in the design document.
 */
object AppUsageSorter {
    
    /**
     * Sorts a list of AppUsageInfo according to the specified requirements:
     * 1. Apps with zero usage time are placed at the bottom
     * 2. Apps are sorted by total usage time in descending order
     * 3. Apps with the same usage time are sorted by last used time in descending order
     * 
     * This ensures consistent sorting behavior across different time filter selections.
     * 
     * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5
     * 
     * @param apps The list of app usage information to sort
     * @return A new sorted list of app usage information
     */
    fun sortByUsage(apps: List<AppUsageInfo>): List<AppUsageInfo> {
        return apps.sortedWith(
            // First criterion: Zero usage apps go to bottom (0 = has usage, 1 = zero usage)
            compareBy<AppUsageInfo> { if (it.totalTimeInForeground == 0L) 1 else 0 }
                // Second criterion: Sort by total usage time in descending order
                .thenByDescending { it.totalTimeInForeground }
                // Third criterion: Sort by last used time in descending order (secondary criteria)
                .thenByDescending { it.lastTimeUsed }
        )
    }
    
}