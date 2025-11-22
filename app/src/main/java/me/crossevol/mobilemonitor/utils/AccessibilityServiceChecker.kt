package me.crossevol.mobilemonitor.utils

import android.content.Context
import android.provider.Settings
import me.crossevol.mobilemonitor.service.AppMonitoringService

/**
 * Utility class to check if the accessibility service is enabled
 */
object AccessibilityServiceChecker {

    /**
     * Check if the app's accessibility service is currently enabled
     * 
     * @param context Application context
     * @return true if the service is enabled, false otherwise
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val serviceName = "${context.packageName}/${AppMonitoringService::class.java.name}"
        
        return try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            
            enabledServices?.contains(serviceName) == true
        } catch (e: Exception) {
            // Return false if we can't determine the status
            false
        }
    }
}
