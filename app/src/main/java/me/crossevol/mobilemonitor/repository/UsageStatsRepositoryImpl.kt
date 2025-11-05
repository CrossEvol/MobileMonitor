package me.crossevol.mobilemonitor.repository

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.crossevol.mobilemonitor.model.AppUsageInfo
import me.crossevol.mobilemonitor.model.TimeFilter
import me.crossevol.mobilemonitor.utils.AppUsageSorter
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Concrete implementation of UsageStatsRepository using Android system services
 */
class UsageStatsRepositoryImpl(
    private val context: Context
) : UsageStatsRepository {
    
    private val usageStatsManager: UsageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }
    
    private val packageManager: PackageManager by lazy {
        context.packageManager
    }
    
    private val appOpsManager: AppOpsManager by lazy {
        context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    }
    
    override suspend fun getUsageStats(timeFilter: TimeFilter): Result<List<AppUsageInfo>> = 
        withContext(Dispatchers.IO) {
            try {
                if (!hasUsageStatsPermission()) {
                    return@withContext Result.failure(SecurityException("Usage access permission not granted"))
                }
                
                val (startTime, endTime) = calculateTimeRange(timeFilter)
                val interval = getUsageStatsInterval(timeFilter)
                val usageStats = usageStatsManager.queryUsageStats(
                    interval,
                    startTime,
                    endTime
                )
                
                if (usageStats.isNullOrEmpty()) {
                    return@withContext Result.success(emptyList())
                }
                
                // Debug: Log the number of raw usage stats
                android.util.Log.d("UsageStats", "Found ${usageStats.size} raw usage stats entries")
                
                val aggregatedStats = aggregateUsageStats(usageStats)
                android.util.Log.d("UsageStats", "Aggregated ${aggregatedStats.size} unique apps")
                
                val appUsageInfoList = buildAppUsageInfoList(aggregatedStats)
                android.util.Log.d("UsageStats", "Built ${appUsageInfoList.size} app usage info entries")
                
                val sortedList = AppUsageSorter.sortByUsage(appUsageInfoList)
                
                Result.success(sortedList)
            } catch (e: SecurityException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(RuntimeException("Failed to retrieve usage statistics", e))
            }
        }
    
    override fun hasUsageStatsPermission(): Boolean {
        return try {
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getAppInfo(packageName: String): AppUsageInfo? = 
        withContext(Dispatchers.IO) {
            try {
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                val appName = packageManager.getApplicationLabel(applicationInfo).toString()
                val icon = packageManager.getApplicationIcon(applicationInfo)
                
                AppUsageInfo(
                    packageName = packageName,
                    appName = appName,
                    icon = icon,
                    lastTimeUsed = 0L,
                    totalTimeInForeground = 0L
                )
            } catch (e: PackageManager.NameNotFoundException) {
                null
            } catch (e: Exception) {
                null
            }
        }
    
    /**
     * Calculates the start and end time range based on the selected time filter
     */
    private fun calculateTimeRange(timeFilter: TimeFilter): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        
        // Create a new calendar instance for start time calculation
        val startCalendar = Calendar.getInstance()
        
        when (timeFilter) {
            TimeFilter.DAILY -> {
                // Get data from start of today
                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)
            }
            TimeFilter.WEEKLY -> {
                // Get data from start of this week
                startCalendar.set(Calendar.DAY_OF_WEEK, startCalendar.firstDayOfWeek)
                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)
            }
            TimeFilter.MONTHLY -> {
                // Get data from start of this month
                startCalendar.set(Calendar.DAY_OF_MONTH, 1)
                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)
            }
            TimeFilter.YEARLY -> {
                // Get data from start of this year
                startCalendar.set(Calendar.DAY_OF_YEAR, 1)
                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)
            }
        }
        
        val startTime = startCalendar.timeInMillis
        
        // For debugging: ensure we're getting a reasonable time range
        val timeDiff = endTime - startTime
        val hours = TimeUnit.MILLISECONDS.toHours(timeDiff)
        
        return Pair(startTime, endTime)
    }
    
    /**
     * Aggregates usage statistics by package name, summing up total usage times
     * Includes apps with zero usage to ensure proper sorting behavior
     */
    private fun aggregateUsageStats(usageStats: List<UsageStats>): Map<String, AggregatedUsageData> {
        return usageStats
            .groupBy { it.packageName }
            .mapValues { (_, stats) ->
                AggregatedUsageData(
                    totalTimeInForeground = stats.sumOf { it.totalTimeInForeground },
                    lastTimeUsed = stats.maxOfOrNull { it.lastTimeUsed } ?: 0L
                )
            }
    }
    
    /**
     * Builds a list of AppUsageInfo from aggregated usage data
     */
    private suspend fun buildAppUsageInfoList(
        aggregatedStats: Map<String, AggregatedUsageData>
    ): List<AppUsageInfo> {
        return aggregatedStats.mapNotNull { (packageName, usageData) ->
            try {
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                
                // Skip apps that are clearly not user-facing (more lenient filtering)
                if (shouldSkipApp(applicationInfo, usageData)) {
                    return@mapNotNull null
                }
                
                val appName = packageManager.getApplicationLabel(applicationInfo).toString()
                val icon: Drawable? = try {
                    packageManager.getApplicationIcon(applicationInfo)
                } catch (e: Exception) {
                    null
                }
                
                AppUsageInfo(
                    packageName = packageName,
                    appName = appName,
                    icon = icon,
                    lastTimeUsed = usageData.lastTimeUsed,
                    totalTimeInForeground = usageData.totalTimeInForeground
                )
            } catch (e: PackageManager.NameNotFoundException) {
                // App might have been uninstalled, skip it
                null
            } catch (e: Exception) {
                // Other errors, skip this app
                null
            }
        }
    }
    

    
    /**
     * Gets the appropriate usage stats interval based on the time filter
     */
    private fun getUsageStatsInterval(timeFilter: TimeFilter): Int {
        return when (timeFilter) {
            TimeFilter.DAILY -> UsageStatsManager.INTERVAL_DAILY
            TimeFilter.WEEKLY -> UsageStatsManager.INTERVAL_WEEKLY
            TimeFilter.MONTHLY -> UsageStatsManager.INTERVAL_MONTHLY
            TimeFilter.YEARLY -> UsageStatsManager.INTERVAL_YEARLY
        }
    }
    
    /**
     * Determines if an app should be skipped from the usage statistics
     * Uses more lenient filtering to include user-installed apps
     */
    private fun shouldSkipApp(applicationInfo: ApplicationInfo, usageData: AggregatedUsageData): Boolean {
        val packageName = applicationInfo.packageName
        
        // Always skip our own app
        if (packageName == context.packageName) {
            return true
        }
        
        // Skip apps with no usage time and no recent usage
        if (usageData.totalTimeInForeground <= 0 && usageData.lastTimeUsed <= 0) {
            return true
        }
        
        // Skip known system processes and services that users don't interact with
        val systemPackagesToSkip = setOf(
            "android",
            "com.android.systemui",
            "com.android.launcher",
            "com.android.inputmethod",
            "com.google.android.gms",
            "com.google.android.gsf",
            "com.android.vending", // Google Play Store background processes
            "com.android.providers",
            "com.android.server"
        )
        
        // Skip if it's a known system package that users don't directly use
        if (systemPackagesToSkip.any { packageName.startsWith(it) }) {
            return true
        }
        
        // Check if the app has a launcher intent (can be opened by user)
        val hasLauncherIntent = try {
            packageManager.getLaunchIntentForPackage(packageName) != null
        } catch (e: Exception) {
            false
        }
        
        // If it's a system app without launcher intent, skip it
        val isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        if (isSystemApp && !hasLauncherIntent) {
            return true
        }
        
        // Include all other apps (user-installed apps and user-facing system apps)
        return false
    }
    
    /**
     * Checks if an app is a system app
     */
    private fun isSystemApp(applicationInfo: ApplicationInfo): Boolean {
        return (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }
    
    /**
     * Debug method to get all apps with usage data (including filtered ones)
     * This can help diagnose filtering issues
     */
    suspend fun getAllAppsWithUsageDebug(timeFilter: TimeFilter): List<String> = 
        withContext(Dispatchers.IO) {
            try {
                if (!hasUsageStatsPermission()) {
                    return@withContext emptyList()
                }
                
                val (startTime, endTime) = calculateTimeRange(timeFilter)
                val interval = getUsageStatsInterval(timeFilter)
                val usageStats = usageStatsManager.queryUsageStats(interval, startTime, endTime)
                
                usageStats?.map { "${it.packageName} - ${it.totalTimeInForeground}ms" } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    
    /**
     * Data class to hold aggregated usage statistics
     */
    private data class AggregatedUsageData(
        val totalTimeInForeground: Long,
        val lastTimeUsed: Long
    )
}