package me.crossevol.mobilemonitor.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
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
import java.util.concurrent.TimeUnit

/**
 * Accessibility service that monitors app launches and enforces usage restrictions
 * 
 * This service runs in the background and intercepts app launch events. When a restricted
 * app is launched, it evaluates all applicable rules and redirects to a blocking screen
 * if any restrictions are violated.
 */
class AppMonitoringService : AccessibilityService() {
    
    companion object {
        private const val TAG = "AppMonitoringService"
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
        Log.d(TAG, "========================================")
        Log.d(TAG, "Accessibility Service CONNECTED")
        Log.d(TAG, "========================================")
        
        // Initialize SharedPreferences for global monitoring setting
        sharedPreferences = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Initialize database and repository
        val database = AppRestrictionDatabase.getDatabase(applicationContext)
        repository = AppRestrictionRepositoryImpl(
            appInfoDao = database.appInfoDao(),
            appRuleDao = database.appRuleDao(),
            context = applicationContext
        )
        
        Log.d(TAG, "Repository initialized")
        
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
        if (event == null) {
            Log.v(TAG, "Received null event")
            return
        }
        
        // Only handle window state changes (app launches)
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            Log.d(TAG, "Window state changed: $packageName")
            
            // Ignore our own package to prevent blocking ourselves
            if (packageName == this.packageName) {
                Log.v(TAG, "Ignoring own package")
                return
            }
            
            Log.i(TAG, "Checking restrictions for: $packageName")
            
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
        Log.w(TAG, "Accessibility Service INTERRUPTED")
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
            Log.d(TAG, "Monitoring enabled: $monitoringEnabled")
            
            // If monitoring is disabled, skip all rule enforcement (Requirement 11.7)
            if (!monitoringEnabled) {
                Log.i(TAG, "Monitoring is disabled - skipping restriction check")
                return
            }
            
            // If monitoring is enabled, enforce all active rules (Requirement 11.8)
            // Check if app is restricted using repository
            Log.d(TAG, "Checking restriction for: $packageName")
            val result = repository.checkRestriction(packageName)
            
            Log.i(TAG, "Restriction result for $packageName: isRestricted=${result.isRestricted}")
            
            if (result.isRestricted) {
                Log.w(TAG, "APP IS RESTRICTED! Launching blocking screen for: ${result.appName}")
                launchBlockingScreen(result)
            } else {
                Log.d(TAG, "App is not restricted: $packageName")
            }
        } catch (e: Exception) {
            // Log error but don't crash the service
            Log.e(TAG, "Error checking restrictions for $packageName", e)
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
        // Check if we have permission to show overlay windows (required for Android 10+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!android.provider.Settings.canDrawOverlays(this)) {
                // Cannot show blocking screen without permission
                // Log this issue but don't crash the service
                android.util.Log.w("AppMonitoringService", 
                    "SYSTEM_ALERT_WINDOW permission not granted. Cannot block app: ${result.appName}")
                return
            }
        }
        
        val intent = Intent(this, me.crossevol.mobilemonitor.ui.BlockingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or 
                     Intent.FLAG_ACTIVITY_CLEAR_TOP or
                     Intent.FLAG_ACTIVITY_NO_HISTORY or
                     Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            putExtra("RESTRICTION_RESULT", result)
        }
        
        startActivity(intent)
    }
    
    /**
     * Loads all enabled rules from the database into the in-memory cache
     * This method should be called when the service starts and whenever rules are updated
     */
    private suspend fun loadRulesIntoCache() {
        try {
            Log.d(TAG, "Loading rules into cache...")
            
            // Get all enabled rules from repository
            val rules = repository.getAllEnabledRules()
            Log.d(TAG, "Found ${rules.size} enabled rules")
            
            // Clear existing cache
            rulesCache.clear()
            
            // Group rules by app and load into cache
            val rulesByApp = rules.groupBy { it.appInfoId }
            
            for ((appId, appRules) in rulesByApp) {
                // Get app info to get package name
                val app = repository.getAppById(appId)
                app?.let {
                    rulesCache[it.packageName] = appRules
                    Log.d(TAG, "Cached ${appRules.size} rules for ${it.packageName}")
                }
            }
            
            Log.i(TAG, "Rules cache loaded: ${rulesCache.size} apps with rules")
        } catch (e: Exception) {
            // Log error but don't crash the service
            Log.e(TAG, "Error loading rules into cache", e)
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
     * Cleans up resources and schedules restart worker
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "========================================")
        Log.w(TAG, "Accessibility Service DESTROYED")
        Log.w(TAG, "========================================")
        
        serviceScope.cancel()
        rulesCache.clear()
        
        // Schedule worker to check if service needs to be restarted
        scheduleServiceRestartWorker()
    }
    
    /**
     * Schedule a WorkManager task to check and restart the service if needed
     */
    private fun scheduleServiceRestartWorker() {
        try {
            val workRequest = OneTimeWorkRequestBuilder<ServiceRestartWorker>()
                .setInitialDelay(5, TimeUnit.SECONDS)
                .build()
            
            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork(
                    ServiceRestartWorker.WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        }
    }
}
