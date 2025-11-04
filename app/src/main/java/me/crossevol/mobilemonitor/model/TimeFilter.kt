package me.crossevol.mobilemonitor.model

/**
 * Enum representing different time filter options for usage statistics
 * 
 * @param displayName Human-readable name for the filter option
 * @param days Number of days to look back from current time
 */
enum class TimeFilter(val displayName: String, val days: Int) {
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30),
    YEARLY("Yearly", 365)
}