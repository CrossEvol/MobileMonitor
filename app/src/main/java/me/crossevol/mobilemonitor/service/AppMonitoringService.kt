package me.crossevol.mobilemonitor.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.crossevol.mobilemonitor.data.database.AppRestrictionDatabase
import me.crossevol.mobilemonitor.model.AppRule
import me.crossevol.mobilemonitor.model.RestrictionResult
import me.crossevol.mobilemonitor.repository.AppRestrictionRepository
import me.crossevol.mobilemonitor.repository.AppRestrictionRepositoryImpl
import java.util.concurrent.ConcurrentHashMap

/**
 * Accessibility service that monitors app launches and enforces usage restrictions
 * 
 * This service runs in the background and intercepts app launch events. When a restricted
 * app is launched, it evaluates all applicable rules and redirects to a blocking screen
 * if any restrictions are violated.
 */
class AppMonitoringService : AccessibilityService() {
    
    companion object {
        private const val PREFS_NAME = "app_monitoring_prefs"
        private const val KEY_MONITORING_ENABLED = "monitoring_enabled"
    }
    
    private lateinit var repository: AppRestrictionRepository
    private lateinit var sharedPreferences: SharedPreferences
    
    /**
     * Cache of rules indexed by package name for fast lookup
     * Key: package name, Value: list of rules for that app
     */
    private val rulesCache = ConcurrentHashMap<String, List<AppRule>>()
    
    /**
     * Coroutine scope for background operations
     */
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    /**
     * Called when the service is connected and ready to use
     * Initializes the repository and loads rules into cache
     */
    override fun onServiceConnected() {
        super.onServiceConnected()
        
        // Initialize SharedPreferences for global monitoring setting
        sharedPreferences = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Initialize database and repository
        val database = AppRestrictionDatabase.getDatabase(applicationContext)
        repository = AppRestrictionRepositoryImpl(
            appInfoDao = database.appInfoDao(),
            appRuleDao = database.appRuleDao(),
            context = applicationContext
        )
        
        // Load rules into cache
        serviceScope.launch {
            loadRulesIntoCache()
        }
    }
    
    /**
     * Called when an accessibility event occurs
     * Detects app launches and enforces restrictions
     * 
     * @param event The accessibility event
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        // Only handle window state changes (app launches)
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Ignore our own package to prevent blocking ourselves
            if (packageName == this.packageName) {
                return
            }
            
            // Check and enforce restrictions in background
            serviceScope.launch {
                checkAndEnforceRestrictions(packageName)
            }
        }
    }
    
    /**
     * Called when the service is interrupted
     * Required by AccessibilityService but not used
     */
    override fun onInterrupt() {
        // Service interrupted - no action needed
    }
    
    /**
     * Checks if an app launch violates any restrictions and enforces them
     * 
     * @param packageName The package name of the launched app
     */
    private suspend fun checkAndEnforceRestrictions(packageName: String) {
        try {
            // Check if global monitoring is enabled
            val monitoringEnabled = isMonitoringEnabled()
            
            // If monitoring is disabled, skip all rule enforcement (Requirement 11.7)
            if (!monitoringEnabled) {
                return
            }
            
            // If monitoring is enabled, enforce all active rules (Requirement 11.8)
            // Check if app is restricted using repository
            val result = repository.checkRestriction(packageName)
            
            if (result.isRestricted) {
                launchBlockingScreen(result)
            }
        } catch (e: Exception) {
            // Log error but don't crash the service
            e.printStackTrace()
        }
    }
    
    /**
     * Checks if global monitoring is enabled
     * 
     * @return true if monitoring is enabled, false otherwise
     */
    private fun isMonitoringEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_MONITORING_ENABLED, true)
    }
    
    /**
     * Launches the blocking screen with restriction information
     * 
     * @param result The restriction result containing violation details
     */
    private fun launchBlockingScreen(result: RestrictionResult) {
        // TODO: Create BlockingActivity and update this intent
        // For now, we'll create a placeholder intent with individual extras
        val intent = Intent().apply {
            // Will be updated to: Intent(this, BlockingActivity::class.java)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("APP_NAME", result.appName)
            putExtra("CURRENT_USAGE_TIME", result.currentUsageTime)
            putExtra("CURRENT_USAGE_COUNT", result.currentUsageCount)
            putExtra("IS_RESTRICTED", result.isRestricted)
            // Rule details if available
            result.violatedRule?.let { rule ->
                putExtra("RULE_ID", rule.id)
                putExtra("RULE_DAY", rule.day.value)
                putExtra("RULE_TIME_START", rule.timeRangeStart.toString())
                putExtra("RULE_TIME_END", rule.timeRangeEnd.toString())
                putExtra("RULE_TOTAL_TIME", rule.totalTime)
                putExtra("RULE_TOTAL_COUNT", rule.totalCount)
            }
        }
        
        // TODO: Uncomment when BlockingActivity is created
        // startActivity(intent)
    }
    
    /**
     * Loads all enabled rules from the database into the in-memory cache
     * This method should be called when the service starts and whenever rules are updated
     */
    private suspend fun loadRulesIntoCache() {
        try {
            // Get all enabled rules from repository
            val rules = repository.getAllEnabledRules()
            
            // Clear existing cache
            rulesCache.clear()
            
            // Group rules by app and load into cache
            val rulesByApp = rules.groupBy { it.appInfoId }
            
            for ((appId, appRules) in rulesByApp) {
                // Get app info to get package name
                val app = repository.getAppById(appId)
                app?.let {
                    rulesCache[it.packageName] = appRules
                }
            }
        } catch (e: Exception) {
            // Log error but don't crash the service
            e.printStackTrace()
        }
    }
    
    /**
     * Reloads rules from the database into the cache
     * This method should be called whenever rules are added, updated, or deleted
     */
    fun reloadRules() {
        serviceScope.launch {
            loadRulesIntoCache()
        }
    }
    
    /**
     * Called when the service is destroyed
     * Cleans up resources
     */
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        rulesCache.clear()
    }
}
