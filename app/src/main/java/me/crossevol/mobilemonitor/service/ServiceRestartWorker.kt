package me.crossevol.mobilemonitor.service

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * WorkManager worker that handles service restart after crashes
 * 
 * This worker is scheduled when the monitoring service crashes or stops unexpectedly.
 * It attempts to guide the user to re-enable the accessibility service.
 */
class ServiceRestartWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Check if accessibility service is enabled
            if (!isAccessibilityServiceEnabled()) {
                // Send notification to user to re-enable service
                sendServiceRestartNotification()
            }
            
            Result.success()
        } catch (e: Exception) {
            // Retry on failure
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    /**
     * Check if the accessibility service is enabled
     */
    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName = "${applicationContext.packageName}/${AppMonitoringService::class.java.name}"
        
        return try {
            val enabledServices = Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            
            enabledServices?.contains(serviceName) == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Send a notification to the user to restart the service
     */
    private fun sendServiceRestartNotification() {
        // Create notification channel if needed
        val notificationHelper = ServiceNotificationHelper(applicationContext)
        notificationHelper.sendServiceRestartNotification()
    }

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
        const val WORK_NAME = "service_restart_worker"
    }
}
