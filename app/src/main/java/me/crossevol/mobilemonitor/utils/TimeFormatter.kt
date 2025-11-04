package me.crossevol.mobilemonitor.utils

/**
 * Utility object for formatting time durations into human-readable strings.
 * Handles formatting of milliseconds into appropriate time units based on duration.
 */
object TimeFormatter {
    
    /**
     * Formats milliseconds into a readable time string.
     * 
     * @param timeInMillis The time duration in milliseconds
     * @return Formatted time string according to the following rules:
     *         - Less than 1 minute: "30s"
     *         - 1 minute to less than 1 hour: "45m"
     *         - 1 hour or more: "2h 45m" or "2h" (if no minutes)
     *         - Zero usage: "0m"
     */
    fun formatUsageTime(timeInMillis: Long): String {
        // Handle zero or negative usage
        if (timeInMillis <= 0) {
            return "0m"
        }
        
        val totalSeconds = timeInMillis / 1000
        
        return when {
            // Less than 1 minute - show seconds
            totalSeconds < 60 -> "${totalSeconds}s"
            
            // Less than 1 hour - show minutes only
            totalSeconds < 3600 -> {
                val minutes = totalSeconds / 60
                "${minutes}m"
            }
            
            // 1 hour or more - show hours and minutes
            else -> {
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                
                if (minutes > 0) {
                    "${hours}h ${minutes}m"
                } else {
                    "${hours}h"
                }
            }
        }
    }
}