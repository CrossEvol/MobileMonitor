package me.crossevol.mobilemonitor.utils

import me.crossevol.mobilemonitor.model.AppRule
import me.crossevol.mobilemonitor.model.DayOfWeek
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalTime

/**
 * Unit tests for RuleCoverageCalculator
 */
class RuleCoverageCalculatorTest {
    
    @Test
    fun `calculateCoverage with empty rules returns all false coverage`() {
        val result = RuleCoverageCalculator.calculateCoverage(emptyList())
        
        // Verify all cells are false (no coverage)
        for (day in 0..6) {
            for (hour in 0..23) {
                assertFalse("Day $day, Hour $hour should not have coverage", 
                           result.hasRuleCoverage(day, hour))
            }
        }
    }
    
    @Test
    fun `calculateCoverage with single rule marks correct time range`() {
        val rule = AppRule(
            id = 1,
            day = DayOfWeek.MONDAY,
            timeRangeStart = LocalTime.of(9, 0),
            timeRangeEnd = LocalTime.of(17, 0),
            totalTime = 480,
            totalCount = 10,
            appInfoId = 1
        )
        
        val result = RuleCoverageCalculator.calculateCoverage(listOf(rule))
        
        // Verify Monday 9:00-17:00 is covered
        for (hour in 9..17) {
            assertTrue("Monday hour $hour should have coverage", 
                      result.hasRuleCoverage(0, hour))
        }
        
        // Verify other hours on Monday are not covered
        for (hour in 0..8) {
            assertFalse("Monday hour $hour should not have coverage", 
                       result.hasRuleCoverage(0, hour))
        }
        for (hour in 18..23) {
            assertFalse("Monday hour $hour should not have coverage", 
                       result.hasRuleCoverage(0, hour))
        }
        
        // Verify other days are not covered
        for (day in 1..6) {
            for (hour in 0..23) {
                assertFalse("Day $day, Hour $hour should not have coverage", 
                           result.hasRuleCoverage(day, hour))
            }
        }
    }
    
    @Test
    fun `calculateCoverage handles midnight crossing correctly`() {
        val rule = AppRule(
            id = 1,
            day = DayOfWeek.FRIDAY,
            timeRangeStart = LocalTime.of(22, 0),
            timeRangeEnd = LocalTime.of(6, 0),
            totalTime = 480,
            totalCount = 10,
            appInfoId = 1
        )
        
        val result = RuleCoverageCalculator.calculateCoverage(listOf(rule))
        
        // Verify Friday 22:00-23:59 is covered
        for (hour in 22..23) {
            assertTrue("Friday hour $hour should have coverage", 
                      result.hasRuleCoverage(4, hour))
        }
        
        // Verify Saturday 00:00-06:00 is covered
        for (hour in 0..6) {
            assertTrue("Saturday hour $hour should have coverage", 
                      result.hasRuleCoverage(5, hour))
        }
    }
    
    @Test
    fun `calculateCoverage handles overlapping rules correctly`() {
        val rule1 = AppRule(
            id = 1,
            day = DayOfWeek.MONDAY,
            timeRangeStart = LocalTime.of(9, 0),
            timeRangeEnd = LocalTime.of(12, 0),
            totalTime = 180,
            totalCount = 5,
            appInfoId = 1
        )
        
        val rule2 = AppRule(
            id = 2,
            day = DayOfWeek.MONDAY,
            timeRangeStart = LocalTime.of(11, 0),
            timeRangeEnd = LocalTime.of(15, 0),
            totalTime = 240,
            totalCount = 8,
            appInfoId = 1
        )
        
        val result = RuleCoverageCalculator.calculateCoverage(listOf(rule1, rule2))
        
        // Verify Monday 9:00-15:00 is covered (union of both rules)
        for (hour in 9..15) {
            assertTrue("Monday hour $hour should have coverage", 
                      result.hasRuleCoverage(0, hour))
        }
        
        // Verify other hours are not covered
        for (hour in 0..8) {
            assertFalse("Monday hour $hour should not have coverage", 
                       result.hasRuleCoverage(0, hour))
        }
        for (hour in 16..23) {
            assertFalse("Monday hour $hour should not have coverage", 
                       result.hasRuleCoverage(0, hour))
        }
    }
    
    @Test
    fun `isValidRule returns true for valid rules`() {
        val validRule = AppRule(
            id = 1,
            day = DayOfWeek.MONDAY,
            timeRangeStart = LocalTime.of(9, 0),
            timeRangeEnd = LocalTime.of(17, 0),
            totalTime = 480,
            totalCount = 10,
            appInfoId = 1
        )
        
        assertTrue("Valid rule should pass validation", 
                  RuleCoverageCalculator.isValidRule(validRule))
    }
}