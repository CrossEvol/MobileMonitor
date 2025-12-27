package me.crossevol.mobilemonitor.ui

import me.crossevol.mobilemonitor.model.AppRule
import me.crossevol.mobilemonitor.model.DayOfWeek
import me.crossevol.mobilemonitor.utils.RuleCoverageCalculator
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalTime

/**
 * Unit tests for TimeGridView data reactivity
 * Tests that the grid properly calculates coverage when rules change
 */
class TimeGridViewTest {
    
    @Test
    fun `TimeGridView calculates coverage correctly for empty rules`() {
        val rules = emptyList<AppRule>()
        val timeGridState = RuleCoverageCalculator.calculateCoverage(rules)
        
        // Verify all cells are uncovered
        for (day in 0..6) {
            for (hour in 0..23) {
                assertFalse("Day $day, Hour $hour should not have coverage with empty rules", 
                           timeGridState.hasRuleCoverage(day, hour))
            }
        }
    }
    
    @Test
    fun `TimeGridView updates coverage when rules are added`() {
        // Start with empty rules
        val emptyRules = emptyList<AppRule>()
        val emptyState = RuleCoverageCalculator.calculateCoverage(emptyRules)
        
        // Verify no coverage initially
        assertFalse("Monday 9AM should not have coverage initially", 
                   emptyState.hasRuleCoverage(0, 9))
        
        // Add a rule
        val newRule = AppRule(
            id = 1,
            day = DayOfWeek.MONDAY,
            timeRangeStart = LocalTime.of(9, 0),
            timeRangeEnd = LocalTime.of(17, 0),
            totalTime = 480,
            totalCount = 10,
            appInfoId = 1
        )
        
        val rulesWithNewRule = listOf(newRule)
        val updatedState = RuleCoverageCalculator.calculateCoverage(rulesWithNewRule)
        
        // Verify coverage is now present
        assertTrue("Monday 9AM should have coverage after adding rule", 
                  updatedState.hasRuleCoverage(0, 9))
        assertTrue("Monday 5PM should have coverage after adding rule", 
                  updatedState.hasRuleCoverage(0, 17))
    }
    
    @Test
    fun `TimeGridView updates coverage when rules are modified`() {
        // Start with a rule covering 9-17
        val originalRule = AppRule(
            id = 1,
            day = DayOfWeek.MONDAY,
            timeRangeStart = LocalTime.of(9, 0),
            timeRangeEnd = LocalTime.of(17, 0),
            totalTime = 480,
            totalCount = 10,
            appInfoId = 1
        )
        
        val originalState = RuleCoverageCalculator.calculateCoverage(listOf(originalRule))
        
        // Verify original coverage
        assertTrue("Monday 9AM should have coverage with original rule", 
                  originalState.hasRuleCoverage(0, 9))
        assertFalse("Monday 8AM should not have coverage with original rule", 
                   originalState.hasRuleCoverage(0, 8))
        
        // Modify rule to cover 8-18
        val modifiedRule = originalRule.copy(
            timeRangeStart = LocalTime.of(8, 0),
            timeRangeEnd = LocalTime.of(18, 0)
        )
        
        val modifiedState = RuleCoverageCalculator.calculateCoverage(listOf(modifiedRule))
        
        // Verify updated coverage
        assertTrue("Monday 8AM should have coverage with modified rule", 
                  modifiedState.hasRuleCoverage(0, 8))
        assertTrue("Monday 6PM should have coverage with modified rule", 
                  modifiedState.hasRuleCoverage(0, 18))
    }
    
    @Test
    fun `TimeGridView updates coverage when rules are removed`() {
        // Start with a rule
        val rule = AppRule(
            id = 1,
            day = DayOfWeek.MONDAY,
            timeRangeStart = LocalTime.of(9, 0),
            timeRangeEnd = LocalTime.of(17, 0),
            totalTime = 480,
            totalCount = 10,
            appInfoId = 1
        )
        
        val stateWithRule = RuleCoverageCalculator.calculateCoverage(listOf(rule))
        
        // Verify coverage exists
        assertTrue("Monday 9AM should have coverage with rule", 
                  stateWithRule.hasRuleCoverage(0, 9))
        
        // Remove the rule
        val stateWithoutRule = RuleCoverageCalculator.calculateCoverage(emptyList())
        
        // Verify coverage is removed
        assertFalse("Monday 9AM should not have coverage after rule removal", 
                   stateWithoutRule.hasRuleCoverage(0, 9))
    }
}