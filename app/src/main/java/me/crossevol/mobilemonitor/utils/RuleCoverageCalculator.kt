package me.crossevol.mobilemonitor.utils

import me.crossevol.mobilemonitor.model.AppRule
import me.crossevol.mobilemonitor.model.DayOfWeek
import me.crossevol.mobilemonitor.model.TimeGridState

/**
 * Utility class for calculating rule coverage across a 7x24 time grid
 * Processes AppRule list into a boolean matrix representing which hours have restrictions
 */
object RuleCoverageCalculator {
    
    /**
     * Calculates rule coverage for a list of app rules
     * 
     * @param rules List of AppRule objects to process
     * @return TimeGridState containing the 7x24 coverage matrix
     */
    fun calculateCoverage(rules: List<AppRule>): TimeGridState {
        val timeGridState = TimeGridState()
        
        // Process each rule and mark covered time slots
        rules.forEach { rule ->
            markRuleCoverage(timeGridState, rule)
        }
        
        return timeGridState
    }
    
    /**
     * Marks coverage for a single rule in the time grid
     * Handles time range expansion and midnight boundary crossings
     * 
     * @param timeGridState The time grid state to update
     * @param rule The rule to process
     */
    private fun markRuleCoverage(timeGridState: TimeGridState, rule: AppRule) {
        val dayIndex = getDayIndex(rule.day)
        val startHour = rule.timeRangeStart.hour
        val endHour = rule.timeRangeEnd.hour
        
        // Handle time ranges that don't cross midnight
        if (rule.timeRangeStart.isBefore(rule.timeRangeEnd) || rule.timeRangeStart == rule.timeRangeEnd) {
            markTimeRange(timeGridState, dayIndex, startHour, endHour)
        } else {
            // Handle time ranges that cross midnight (e.g., 22:00 to 06:00)
            // Mark from start hour to end of day (23:59)
            markTimeRange(timeGridState, dayIndex, startHour, 23)
            
            // Mark from start of next day (00:00) to end hour
            val nextDayIndex = (dayIndex + 1) % 7
            markTimeRange(timeGridState, nextDayIndex, 0, endHour)
        }
    }
    
    /**
     * Marks a time range as covered in the grid
     * 
     * @param timeGridState The time grid state to update
     * @param dayIndex Day index (0-6)
     * @param startHour Start hour (inclusive)
     * @param endHour End hour (inclusive)
     */
    private fun markTimeRange(timeGridState: TimeGridState, dayIndex: Int, startHour: Int, endHour: Int) {
        for (hour in startHour..endHour) {
            timeGridState.setRuleCoverage(dayIndex, hour, true)
        }
    }
    
    /**
     * Converts DayOfWeek enum to array index
     * 
     * @param day DayOfWeek enum value
     * @return Array index (0-6 for Monday-Sunday)
     */
    private fun getDayIndex(day: DayOfWeek): Int {
        return when (day) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.THURSDAY -> 3
            DayOfWeek.FRIDAY -> 4
            DayOfWeek.SATURDAY -> 5
            DayOfWeek.SUNDAY -> 6
        }
    }
    
    /**
     * Validates that a rule has valid time range
     * 
     * @param rule The rule to validate
     * @return true if the rule has a valid time range, false otherwise
     */
    fun isValidRule(rule: AppRule): Boolean {
        // LocalTime in Kotlin is always valid, so we just need to check basic constraints
        return rule.timeRangeStart.hour in 0..23 && 
               rule.timeRangeStart.minute in 0..59 &&
               rule.timeRangeEnd.hour in 0..23 && 
               rule.timeRangeEnd.minute in 0..59
    }
}