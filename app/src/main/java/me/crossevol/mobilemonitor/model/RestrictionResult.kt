package me.crossevol.mobilemonitor.model

/**
 * Result of checking if an app is restricted
 * 
 * @param isRestricted Whether the app is currently restricted
 * @param appName Name of the app being checked
 * @param violatedRule The rule that was violated, if any
 * @param currentUsageTime Current usage time in minutes
 * @param currentUsageCount Current access count
 */
data class RestrictionResult(
    val isRestricted: Boolean,
    val appName: String,
    val violatedRule: AppRule?,
    val currentUsageTime: Int, // minutes
    val currentUsageCount: Int
)
