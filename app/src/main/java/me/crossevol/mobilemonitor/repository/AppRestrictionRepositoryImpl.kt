package me.crossevol.mobilemonitor.repository

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.crossevol.mobilemonitor.data.dao.AppInfoDao
import me.crossevol.mobilemonitor.data.dao.AppRuleDao
import me.crossevol.mobilemonitor.data.entity.AppInfoEntity
import me.crossevol.mobilemonitor.data.entity.AppRuleEntity
import me.crossevol.mobilemonitor.model.AppInfo
import me.crossevol.mobilemonitor.model.AppRule
import me.crossevol.mobilemonitor.model.DayOfWeek
import me.crossevol.mobilemonitor.model.RestrictionResult
import me.crossevol.mobilemonitor.utils.UsageTracker
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * Custom exception for database operation failures
 */
class DatabaseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Custom exception for validation failures
 */
class ValidationException(message: String) : Exception(message)

/**
 * Implementation of AppRestrictionRepository
 */
class AppRestrictionRepositoryImpl(
    private val appInfoDao: AppInfoDao,
    private val appRuleDao: AppRuleDao,
    private val context: Context
) : AppRestrictionRepository {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val usageTracker = UsageTracker(context)

    // Entity to Domain mapping
    private fun AppInfoEntity.toDomain(): AppInfo {
        val packageManager = context.packageManager
        val icon = try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        
        return AppInfo(
            id = id,
            appName = appName,
            packageName = packageName,
            enabled = enabled,
            icon = icon,
            lastTimeUsed = 0, // Will be populated from UsageStatsManager
            totalTimeInForeground = 0, // Will be populated from UsageStatsManager
            createdTime = createdTime
        )
    }

    private fun AppRuleEntity.toDomain(): AppRule {
        return AppRule(
            id = id,
            day = DayOfWeek.fromValue(day) ?: DayOfWeek.MONDAY,
            timeRangeStart = LocalTime.parse(timeRangeStart, timeFormatter),
            timeRangeEnd = LocalTime.parse(timeRangeEnd, timeFormatter),
            totalTime = totalTime,
            totalCount = totalCount,
            appInfoId = appInfoId,
            createdTime = createdTime
        )
    }

    // Domain to Entity mapping
    private fun AppInfo.toEntity(): AppInfoEntity {
        return AppInfoEntity(
            id = id,
            appName = appName,
            packageName = packageName,
            enabled = enabled,
            createdTime = createdTime
        )
    }

    private fun AppRule.toEntity(): AppRuleEntity {
        return AppRuleEntity(
            id = id,
            day = day.value,
            timeRangeStart = timeRangeStart.format(timeFormatter),
            timeRangeEnd = timeRangeEnd.format(timeFormatter),
            totalTime = totalTime,
            totalCount = totalCount,
            appInfoId = appInfoId,
            createdTime = createdTime
        )
    }

    // App operations
    override fun getAllApps(): Flow<List<AppInfo>> {
        return appInfoDao.getAllApps().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAppById(appId: Long): AppInfo? {
        return try {
            appInfoDao.getAppById(appId)?.toDomain()
        } catch (e: Exception) {
            // Log error and rethrow with more context
            throw DatabaseException("Failed to get app by ID: $appId", e)
        }
    }

    override suspend fun getAppByPackageName(packageName: String): AppInfo? {
        return try {
            appInfoDao.getAppByPackageName(packageName)?.toDomain()
        } catch (e: Exception) {
            throw DatabaseException("Failed to get app by package name: $packageName", e)
        }
    }

    override suspend fun saveApp(app: AppInfo): Long {
        return try {
            appInfoDao.insertApp(app.toEntity())
        } catch (e: Exception) {
            throw DatabaseException("Failed to save app: ${app.appName}", e)
        }
    }

    override suspend fun updateAppEnabled(appId: Long, enabled: Boolean) {
        try {
            val app = appInfoDao.getAppById(appId)
            app?.let {
                appInfoDao.updateApp(it.copy(enabled = enabled))
            } ?: throw DatabaseException("App not found with ID: $appId")
        } catch (e: DatabaseException) {
            throw e
        } catch (e: Exception) {
            throw DatabaseException("Failed to update app enabled status", e)
        }
    }

    override suspend fun deleteApp(appId: Long) {
        try {
            val app = appInfoDao.getAppById(appId)
            app?.let {
                appInfoDao.deleteApp(it)
            } ?: throw DatabaseException("App not found with ID: $appId")
        } catch (e: DatabaseException) {
            throw e
        } catch (e: Exception) {
            throw DatabaseException("Failed to delete app", e)
        }
    }

    // Rule operations
    override fun getRulesForApp(appId: Long): Flow<List<AppRule>> {
        return appRuleDao.getRulesForApp(appId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRuleById(ruleId: Long): AppRule? {
        return try {
            appRuleDao.getRuleById(ruleId)?.toDomain()
        } catch (e: Exception) {
            throw DatabaseException("Failed to get rule by ID: $ruleId", e)
        }
    }

    override suspend fun getAllEnabledRules(): List<AppRule> {
        return try {
            appRuleDao.getAllEnabledRules().map { it.toDomain() }
        } catch (e: Exception) {
            throw DatabaseException("Failed to get enabled rules", e)
        }
    }

    override suspend fun saveRule(rule: AppRule): Long {
        return try {
            appRuleDao.insertRule(rule.toEntity())
        } catch (e: Exception) {
            throw DatabaseException("Failed to save rule", e)
        }
    }

    override suspend fun saveRules(rules: List<AppRule>) {
        try {
            if (rules.isEmpty()) {
                throw ValidationException("Cannot save empty rule list")
            }
            appRuleDao.insertRules(rules.map { it.toEntity() })
        } catch (e: ValidationException) {
            throw e
        } catch (e: Exception) {
            throw DatabaseException("Failed to save rules", e)
        }
    }

    override suspend fun updateRule(rule: AppRule) {
        try {
            appRuleDao.updateRule(rule.toEntity())
        } catch (e: Exception) {
            throw DatabaseException("Failed to update rule", e)
        }
    }

    override suspend fun deleteRule(ruleId: Long) {
        try {
            val rule = appRuleDao.getRuleById(ruleId)
            rule?.let {
                appRuleDao.deleteRule(it)
            } ?: throw DatabaseException("Rule not found with ID: $ruleId")
        } catch (e: DatabaseException) {
            throw e
        } catch (e: Exception) {
            throw DatabaseException("Failed to delete rule", e)
        }
    }

    // Monitoring operations
    override suspend fun checkRestriction(packageName: String): RestrictionResult {
        return try {
            checkRestrictionInternal(packageName)
        } catch (e: Exception) {
            // Log error but return non-restricted result to prevent blocking user
            // This ensures service doesn't crash on errors
            RestrictionResult(
                isRestricted = false,
                appName = packageName,
                violatedRule = null,
                currentUsageTime = 0,
                currentUsageCount = 0
            )
        }
    }
    
    private suspend fun checkRestrictionInternal(packageName: String): RestrictionResult {
        // Get app info
        val app = getAppByPackageName(packageName)
        
        // If app doesn't exist or is disabled, no restriction
        if (app == null || !app.enabled) {
            return RestrictionResult(
                isRestricted = false,
                appName = app?.appName ?: packageName,
                violatedRule = null,
                currentUsageTime = 0,
                currentUsageCount = 0
            )
        }

        // Get current day and time
        val calendar = Calendar.getInstance()
        val currentDay = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
            else -> DayOfWeek.MONDAY
        }
        val currentTime = LocalTime.now()

        // Get all rule entities for this app and convert to domain
        val ruleEntities = appRuleDao.getRulesForApp(app.id)
        
        // We need to get the list synchronously - use a query that returns List instead of Flow
        // For now, we'll get all enabled rules and filter by app
        val allRules = getAllEnabledRules()
        val appRules = allRules.filter { it.appInfoId == app.id }
        
        // Check each rule for violations
        for (rule in appRules) {
            // Check if rule applies to current day
            if (rule.day != currentDay) {
                continue
            }
            
            // Check if current time is within rule's time range
            val isInTimeRange = if (rule.timeRangeEnd.isBefore(rule.timeRangeStart)) {
                // Time range crosses midnight
                currentTime.isAfter(rule.timeRangeStart) || currentTime.isBefore(rule.timeRangeEnd)
            } else {
                // Normal time range
                currentTime.isAfter(rule.timeRangeStart) && currentTime.isBefore(rule.timeRangeEnd)
            }
            
            if (!isInTimeRange) {
                continue
            }
            
            // Get actual usage stats from UsageStatsManager
            val (currentUsageTime, currentUsageCount) = usageTracker.getUsageInMinutes(
                packageName,
                rule.timeRangeStart,
                rule.timeRangeEnd
            )
            
            // Check if usage exceeds limits
            val timeExceeded = rule.totalTime > 0 && currentUsageTime >= rule.totalTime
            val countExceeded = rule.totalCount > 0 && currentUsageCount >= rule.totalCount
            
            if (timeExceeded || countExceeded) {
                return RestrictionResult(
                    isRestricted = true,
                    appName = app.appName,
                    violatedRule = rule,
                    currentUsageTime = currentUsageTime,
                    currentUsageCount = currentUsageCount
                )
            }
        }
        
        // No restrictions violated
        return RestrictionResult(
            isRestricted = false,
            appName = app.appName,
            violatedRule = null,
            currentUsageTime = 0,
            currentUsageCount = 0
        )
    }
}
