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
                val usageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, // TODO: 这里为什么使用  INTERVAL_DAILY, 而不是 switch-case 来选择呢?
                    startTime,
                    endTime
                )
                
                if (usageStats.isNullOrEmpty()) {
                    return@withContext Result.success(emptyList())
                }
                
                val aggregatedStats = aggregateUsageStats(usageStats)
                val appUsageInfoList = buildAppUsageInfoList(aggregatedStats)
                val sortedList = sortAppsByUsage(appUsageInfoList)
                
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
        
        when (timeFilter) {
            TimeFilter.DAILY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimeFilter.WEEKLY -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimeFilter.MONTHLY -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimeFilter.YEARLY -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
        }
        
        val startTime = calendar.timeInMillis
        return Pair(startTime, endTime)
    }
    
    /**
     * Aggregates usage statistics by package name, summing up total usage times
     */
    private fun aggregateUsageStats(usageStats: List<UsageStats>): Map<String, AggregatedUsageData> {
        return usageStats
            .filter { it.totalTimeInForeground > 0 } // Filter out apps with no usage
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
                
                // Skip system apps that are not user-facing
                if (isSystemApp(applicationInfo) && !isUserFacingSystemApp(applicationInfo)) {
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
     * Sorts apps by total usage time (descending), then by last used time (descending)
     */
    private fun sortAppsByUsage(apps: List<AppUsageInfo>): List<AppUsageInfo> {
        return apps.sortedWith(
            compareByDescending<AppUsageInfo> { it.totalTimeInForeground }
                .thenByDescending { it.lastTimeUsed }
        )
    }
    
    /**
     * Checks if an app is a system app
     */
    private fun isSystemApp(applicationInfo: ApplicationInfo): Boolean {
        return (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }
    
    /**
     * Checks if a system app is user-facing (like Settings, Phone, etc.)
     */
    private fun isUserFacingSystemApp(applicationInfo: ApplicationInfo): Boolean {
        // Include system apps that have a launcher activity (user can open them)
        return try {
            val intent = packageManager.getLaunchIntentForPackage(applicationInfo.packageName)
            intent != null
        } catch (e: Exception) {
            false
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