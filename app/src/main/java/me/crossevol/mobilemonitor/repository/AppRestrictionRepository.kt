package me.crossevol.mobilemonitor.repository

import kotlinx.coroutines.flow.Flow
import me.crossevol.mobilemonitor.model.AppInfo
import me.crossevol.mobilemonitor.model.AppRule
import me.crossevol.mobilemonitor.model.RestrictionResult

/**
 * Repository interface for app restriction operations
 */
interface AppRestrictionRepository {
    // App operations
    fun getAllApps(): Flow<List<AppInfo>>
    suspend fun getAppById(appId: Long): AppInfo?
    suspend fun getAppByPackageName(packageName: String): AppInfo?
    suspend fun saveApp(app: AppInfo): Long
    suspend fun updateAppEnabled(appId: Long, enabled: Boolean)
    suspend fun deleteApp(appId: Long)
    
    // Rule operations
    fun getRulesForApp(appId: Long): Flow<List<AppRule>>
    suspend fun getRuleById(ruleId: Long): AppRule?
    suspend fun getAllEnabledRules(): List<AppRule>
    suspend fun saveRule(rule: AppRule): Long
    suspend fun saveRules(rules: List<AppRule>)
    suspend fun updateRule(rule: AppRule)
    suspend fun deleteRule(ruleId: Long)
    suspend fun deleteAllRulesForApp(appId: Long)
    
    // Monitoring operations
    suspend fun checkRestriction(packageName: String): RestrictionResult
}
