package me.crossevol.mobilemonitor.utils

import me.crossevol.mobilemonitor.model.AppRule
import me.crossevol.mobilemonitor.model.DayOfWeek
import me.crossevol.mobilemonitor.model.DayPattern
import java.time.LocalTime

/**
 * Utility class for expanding day patterns into individual rules
 */
object RulePatternExpander {
    
    /**
     * Expands a day pattern into a list of rules
     * 
     * @param pattern The day pattern to expand (WORKDAY, WEEKEND, or CUSTOM)
     * @param selectedDays The specific days selected (only used for CUSTOM pattern)
     * @param timeRangeStart Start time of the restriction period
     * @param timeRangeEnd End time of the restriction period
     * @param totalTime Maximum allowed usage time in minutes
     * @param totalCount Maximum allowed access count
     * @param appInfoId The app ID these rules apply to
     * @return List of AppRule objects, one for each day in the pattern
     */
    fun expandPattern(
        pattern: DayPattern,
        selectedDays: Set<DayOfWeek>,
        timeRangeStart: LocalTime,
        timeRangeEnd: LocalTime,
        totalTime: Int,
        totalCount: Int,
        appInfoId: Long
    ): List<AppRule> {
        val days = when (pattern) {
            DayPattern.WORKDAY -> expandWorkday()
            DayPattern.WEEKEND -> expandWeekend()
            DayPattern.CUSTOM -> expandCustom(selectedDays)
        }
        
        return days.map { day ->
            AppRule(
                id = 0, // Will be assigned by database
                day = day,
                timeRangeStart = timeRangeStart,
                timeRangeEnd = timeRangeEnd,
                totalTime = totalTime,
                totalCount = totalCount,
                appInfoId = appInfoId,
                createdTime = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Expands workday pattern to Monday through Friday
     * 
     * @return List of DayOfWeek for Monday through Friday
     */
    private fun expandWorkday(): List<DayOfWeek> {
        return listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
    }
    
    /**
     * Expands weekend pattern to Saturday and Sunday
     * 
     * @return List of DayOfWeek for Saturday and Sunday
     */
    private fun expandWeekend(): List<DayOfWeek> {
        return listOf(
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        )
    }
    
    /**
     * Expands custom pattern to the selected days
     * 
     * @param selectedDays Set of days selected by the user
     * @return List of DayOfWeek from the selected days
     */
    private fun expandCustom(selectedDays: Set<DayOfWeek>): List<DayOfWeek> {
        return selectedDays.toList()
    }
    
    /**
     * Validates that required fields are present for rule creation
     * 
     * @param pattern The day pattern
     * @param selectedDays The selected days (for CUSTOM pattern)
     * @return true if validation passes, false otherwise
     */
    fun validateRuleInput(
        pattern: DayPattern,
        selectedDays: Set<DayOfWeek>
    ): Boolean {
        // Day pattern must be specified
        // For custom pattern, at least one day must be selected
        return when (pattern) {
            DayPattern.CUSTOM -> selectedDays.isNotEmpty()
            else -> true
        }
    }
}
