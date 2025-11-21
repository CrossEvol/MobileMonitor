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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * Implementation of AppRestrictionRepository
 */
class AppRestrictionRepositoryImpl(
    private val appInfoDao: AppInfoDao,
    private val appRuleDao: AppRuleDao,
    private val context: Context
) : AppRestrictionRepository {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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
        return appInfoDao.getAppById(appId)?.toDomain()
    }

    override suspend fun getAppByPackageName(packageName: String): AppInfo? {
        return appInfoDao.getAppByPackageName(packageName)?.toDomain()
    }

    override suspend fun saveApp(app: AppInfo): Long {
        return appInfoDao.insertApp(app.toEntity())
    }

    override suspend fun updateAppEnabled(appId: Long, enabled: Boolean) {
        val app = appInfoDao.getAppById(appId)
        app?.let {
            appInfoDao.updateApp(it.copy(enabled = enabled))
        }
    }

    override suspend fun deleteApp(appId: Long) {
        val app = appInfoDao.getAppById(appId)
        app?.let {
            appInfoDao.deleteApp(it)
        }
    }

    // Rule operations
    override fun getRulesForApp(appId: Long): Flow<List<AppRule>> {
        return appRuleDao.getRulesForApp(appId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRuleById(ruleId: Long): AppRule? {
        return appRuleDao.getRuleById(ruleId)?.toDomain()
    }

    override suspend fun getAllEnabledRules(): List<AppRule> {
        return appRuleDao.getAllEnabledRules().map { it.toDomain() }
    }

    override suspend fun saveRule(rule: AppRule): Long {
        return appRuleDao.insertRule(rule.toEntity())
    }

    override suspend fun saveRules(rules: List<AppRule>) {
        appRuleDao.insertRules(rules.map { it.toEntity() })
    }

    override suspend fun updateRule(rule: AppRule) {
        appRuleDao.updateRule(rule.toEntity())
    }

    override suspend fun deleteRule(ruleId: Long) {
        val rule = appRuleDao.getRuleById(ruleId)
        rule?.let {
            appRuleDao.deleteRule(it)
        }
    }

    // Monitoring operations
    override suspend fun checkRestriction(packageName: String): RestrictionResult {
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
            
            // TODO: Get actual usage stats from UsageStatsManager
            // For now, we'll use placeholder values
            val currentUsageTime = 0 // minutes
            val currentUsageCount = 0
            
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
