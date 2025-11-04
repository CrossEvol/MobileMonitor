package me.crossevol.mobilemonitor.utils

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for TimeFormatter utility class.
 * Tests all formatting scenarios including seconds, minutes, hours, and edge cases.
 */
class TimeFormatterTest {

    @Test
    fun formatUsageTime_zeroMillis_returnsZeroMinutes() {
        val result = TimeFormatter.formatUsageTime(0)
        assertEquals("0m", result)
    }

    @Test
    fun formatUsageTime_negativeMillis_returnsZeroMinutes() {
        val result = TimeFormatter.formatUsageTime(-1000)
        assertEquals("0m", result)
    }

    @Test
    fun formatUsageTime_lessThanOneMinute_returnsSeconds() {
        // 30 seconds
        val result30s = TimeFormatter.formatUsageTime(30 * 1000)
        assertEquals("30s", result30s)
        
        // 1 second
        val result1s = TimeFormatter.formatUsageTime(1000)
        assertEquals("1s", result1s)
        
        // 59 seconds
        val result59s = TimeFormatter.formatUsageTime(59 * 1000)
        assertEquals("59s", result59s)
    }

    @Test
    fun formatUsageTime_exactlyOneMinute_returnsMinutes() {
        val result = TimeFormatter.formatUsageTime(60 * 1000)
        assertEquals("1m", result)
    }

    @Test
    fun formatUsageTime_minutesOnly_returnsMinutes() {
        // 5 minutes
        val result5m = TimeFormatter.formatUsageTime(5 * 60 * 1000)
        assertEquals("5m", result5m)
        
        // 45 minutes
        val result45m = TimeFormatter.formatUsageTime(45 * 60 * 1000)
        assertEquals("45m", result45m)
        
        // 59 minutes
        val result59m = TimeFormatter.formatUsageTime(59 * 60 * 1000)
        assertEquals("59m", result59m)
    }

    @Test
    fun formatUsageTime_exactlyOneHour_returnsHoursOnly() {
        val result = TimeFormatter.formatUsageTime(60 * 60 * 1000)
        assertEquals("1h", result)
    }

    @Test
    fun formatUsageTime_hoursOnly_returnsHoursOnly() {
        // 2 hours exactly
        val result2h = TimeFormatter.formatUsageTime(2 * 60 * 60 * 1000)
        assertEquals("2h", result2h)
        
        // 5 hours exactly
        val result5h = TimeFormatter.formatUsageTime(5 * 60 * 60 * 1000)
        assertEquals("5h", result5h)
    }

    @Test
    fun formatUsageTime_hoursAndMinutes_returnsHoursAndMinutes() {
        // 1 hour 30 minutes
        val result1h30m = TimeFormatter.formatUsageTime((1 * 60 + 30) * 60 * 1000)
        assertEquals("1h 30m", result1h30m)
        
        // 2 hours 45 minutes
        val result2h45m = TimeFormatter.formatUsageTime((2 * 60 + 45) * 60 * 1000)
        assertEquals("2h 45m", result2h45m)
        
        // 10 hours 5 minutes
        val result10h5m = TimeFormatter.formatUsageTime((10 * 60 + 5) * 60 * 1000)
        assertEquals("10h 5m", result10h5m)
    }

    @Test
    fun formatUsageTime_largeValues_handlesCorrectly() {
        // 24 hours
        val result24h = TimeFormatter.formatUsageTime(24 * 60 * 60 * 1000)
        assertEquals("24h", result24h)
        
        // 25 hours 30 minutes
        val result25h30m = TimeFormatter.formatUsageTime((25 * 60 + 30) * 60 * 1000)
        assertEquals("25h 30m", result25h30m)
    }

    @Test
    fun formatUsageTime_edgeCases_handlesCorrectly() {
        // Just under 1 minute (59.9 seconds)
        val resultJustUnder1m = TimeFormatter.formatUsageTime(59900)
        assertEquals("59s", resultJustUnder1m)
        
        // Just over 1 minute (60.1 seconds)
        val resultJustOver1m = TimeFormatter.formatUsageTime(60100)
        assertEquals("1m", resultJustOver1m)
        
        // Just under 1 hour (59 minutes 59 seconds)
        val resultJustUnder1h = TimeFormatter.formatUsageTime((59 * 60 + 59) * 1000)
        assertEquals("59m", resultJustUnder1h)
        
        // Just over 1 hour (60 minutes 1 second)
        val resultJustOver1h = TimeFormatter.formatUsageTime((60 * 60 + 1) * 1000)
        assertEquals("1h", resultJustOver1h)
    }
}