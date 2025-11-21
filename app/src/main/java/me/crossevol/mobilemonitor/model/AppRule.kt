package me.crossevol.mobilemonitor.model

import java.time.LocalTime

/**
 * Domain model representing a usage restriction rule
 * 
 * @param id Unique identifier for the rule
 * @param day Day of week when rule applies
 * @param timeRangeStart Start time of restriction period
 * @param timeRangeEnd End time of restriction period
 * @param totalTime Maximum allowed usage time in minutes
 * @param totalCount Maximum allowed access count
 * @param appInfoId Associated app identifier
 * @param createdTime Timestamp when rule was created
 */
data class AppRule(
    val id: Long = 0,
    val day: DayOfWeek,
    val timeRangeStart: LocalTime,
    val timeRangeEnd: LocalTime,
    val totalTime: Int, // minutes
    val totalCount: Int,
    val appInfoId: Long,
    val createdTime: Long = System.currentTimeMillis()
)
