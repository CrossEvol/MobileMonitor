package me.crossevol.mobilemonitor.utils

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import java.time.LocalTime
import java.util.Calendar

/**
 * Tracks app usage time and count using UsageStatsManager
 * 
 * This class provides functionality to:
 * - Query historical usage data from UsageStatsManager
 * - Calculate usage within specific time ranges
 * - Handle time ranges that cross midnight
 * - Count app launches within time periods
 */
class UsageTracker(private val context: Context) {

    private val usageStatsManager: UsageStatsManager? by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    /**
     * Data class representing usage statistics for a specific time period
     */
    data class UsageData(
        val totalTimeInForeground: Long, // milliseconds
        val launchCount: Int
    )

    /**
     * Get current usage for a package within a specific time range today
     * 
     * @param packageName The package to query
     * @param timeRangeStart Start time of the restriction period
     * @param timeRangeEnd End time of the restriction period
     * @return UsageData containing time and count, or null if data unavailable
     */
    fun getCurrentUsage(
        packageName: String,
        timeRangeStart: LocalTime,
        timeRangeEnd: LocalTime
    ): UsageData? {
        val usageStats = usageStatsManager ?: return null

        // Calculate the time range boundaries for today
        val (startMillis, endMillis) = calculateTimeRangeBoundaries(timeRangeStart, timeRangeEnd)

        // Query usage stats for the time range
        val stats = usageStats.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startMillis,
            endMillis
        )

        // Find stats for the specific package
        val packageStats = stats?.firstOrNull { it.packageName == packageName }
            ?: return UsageData(0, 0)

        return UsageData(
            totalTimeInForeground = packageStats.totalTimeInForeground,
            launchCount = packageStats.lastTimeUsed.let { if (it > 0) 1 else 0 } // Simplified count
        )
    }

    /**
     * Get usage for a package within a time range, with more accurate launch counting
     * 
     * @param packageName The package to query
     * @param timeRangeStart Start time of the restriction period
     * @param timeRangeEnd End time of the restriction period
     * @return UsageData containing time in minutes and launch count
     */
    fun getUsageInTimeRange(
        packageName: String,
        timeRangeStart: LocalTime,
        timeRangeEnd: LocalTime
    ): UsageData {
        val usageStats = usageStatsManager ?: return UsageData(0, 0)

        val (startMillis, endMillis) = calculateTimeRangeBoundaries(timeRangeStart, timeRangeEnd)

        // Query usage stats
        val stats = usageStats.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startMillis,
            endMillis
        )

        if (stats.isNullOrEmpty()) {
            return UsageData(0, 0)
        }

        // Find the package stats
        val packageStats = stats.firstOrNull { it.packageName == packageName }
            ?: return UsageData(0, 0)

        // Query events for more accurate launch counting
        val events = usageStats.queryEvents(startMillis, endMillis)
        var launchCount = 0

        while (events.hasNextEvent()) {
            val event = android.app.usage.UsageEvents.Event()
            events.getNextEvent(event)

            if (event.packageName == packageName && 
                event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED) {
                launchCount++
            }
        }

        return UsageData(
            totalTimeInForeground = packageStats.totalTimeInForeground,
            launchCount = launchCount
        )
    }

    /**
     * Calculate time range boundaries in milliseconds, handling midnight crossing
     * 
     * @param timeRangeStart Start time
     * @param timeRangeEnd End time
     * @return Pair of (startMillis, endMillis)
     */
    private fun calculateTimeRangeBoundaries(
        timeRangeStart: LocalTime,
        timeRangeEnd: LocalTime
    ): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Set calendar to start time today
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timeRangeStart.hour)
            set(Calendar.MINUTE, timeRangeStart.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timeRangeEnd.hour)
            set(Calendar.MINUTE, timeRangeEnd.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Handle midnight crossing
        if (timeRangeEnd.isBefore(timeRangeStart)) {
            // Time range crosses midnight
            val currentTime = LocalTime.now()
            
            if (currentTime.isBefore(timeRangeEnd)) {
                // We're in the "after midnight" portion
                // Start time was yesterday
                startCalendar.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                // We're in the "before midnight" portion
                // End time is tomorrow
                endCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        return Pair(startCalendar.timeInMillis, endCalendar.timeInMillis)
    }

    /**
     * Check if current time is within a time range, handling midnight crossing
     * 
     * @param timeRangeStart Start time
     * @param timeRangeEnd End time
     * @return true if current time is within range
     */
    fun isInTimeRange(timeRangeStart: LocalTime, timeRangeEnd: LocalTime): Boolean {
        val currentTime = LocalTime.now()
        
        return if (timeRangeEnd.isBefore(timeRangeStart)) {
            // Time range crosses midnight
            currentTime.isAfter(timeRangeStart) || currentTime.isBefore(timeRangeEnd)
        } else {
            // Normal time range
            currentTime.isAfter(timeRangeStart) && currentTime.isBefore(timeRangeEnd)
        }
    }

    /**
     * Get usage in minutes for easier comparison with rule limits
     * 
     * @param packageName The package to query
     * @param timeRangeStart Start time of the restriction period
     * @param timeRangeEnd End time of the restriction period
     * @return UsageData with time converted to minutes
     */
    fun getUsageInMinutes(
        packageName: String,
        timeRangeStart: LocalTime,
        timeRangeEnd: LocalTime
    ): Pair<Int, Int> {
        val usage = getUsageInTimeRange(packageName, timeRangeStart, timeRangeEnd)
        val minutes = (usage.totalTimeInForeground / 60000).toInt() // Convert ms to minutes
        return Pair(minutes, usage.launchCount)
    }
}
