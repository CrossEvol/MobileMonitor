package me.crossevol.mobilemonitor.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.set
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import me.crossevol.mobilemonitor.model.DayOfWeek
import me.crossevol.mobilemonitor.model.DayPattern
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalTime

/**
 * Feature: app-usage-restriction, Property 8: Custom pattern creates rules for selected days
 * Validates: Requirements 3.7
 * 
 * For any set of selected days, the custom pattern should create exactly one rule
 * per selected day with identical time ranges and restrictions.
 */
@RunWith(AndroidJUnit4::class)
class CustomPatternPropertyTest {
    
    // Generator for DayOfWeek
    private fun dayOfWeekArb() = arbitrary {
        DayOfWeek.values()[Arb.int(0..6).bind()]
    }
    
    // Generator for custom pattern parameters
    private fun customPatternParametersArb() = arbitrary {
        val startHour = Arb.int(0..23).bind()
        val startMinute = Arb.int(0..59).bind()
        val endHour = Arb.int(0..23).bind()
        val endMinute = Arb.int(0..59).bind()
        
        CustomPatternParameters(
            selectedDays = Arb.set(dayOfWeekArb(), 1..7).bind(),
            timeRangeStart = LocalTime.of(startHour, startMinute),
            timeRangeEnd = LocalTime.of(endHour, endMinute),
            totalTime = Arb.int(0..1440).bind(),
            totalCount = Arb.int(0..100).bind(),
            appInfoId = Arb.long(1L..1000L).bind()
        )
    }
    
    data class CustomPatternParameters(
        val selectedDays: Set<DayOfWeek>,
        val timeRangeStart: LocalTime,
        val timeRangeEnd: LocalTime,
        val totalTime: Int,
        val totalCount: Int,
        val appInfoId: Long
    )
    
    @Test
    fun propertyTest_CustomPatternCreatesRulesForSelectedDays(): Unit = runBlocking {
        checkAll(100, customPatternParametersArb()) { params ->
            // Expand custom pattern
            val rules = RulePatternExpander.expandPattern(
                pattern = DayPattern.CUSTOM,
                selectedDays = params.selectedDays,
                timeRangeStart = params.timeRangeStart,
                timeRangeEnd = params.timeRangeEnd,
                totalTime = params.totalTime,
                totalCount = params.totalCount,
                appInfoId = params.appInfoId
            )
            
            // Verify exactly one rule per selected day
            assertEquals(
                "Custom pattern should create exactly one rule per selected day",
                params.selectedDays.size,
                rules.size
            )
            
            // Verify rules are for the selected days
            val actualDays = rules.map { it.day }.toSet()
            assertEquals(
                "Custom pattern should create rules for exactly the selected days",
                params.selectedDays,
                actualDays
            )
            
            // Verify all rules have identical settings
            rules.forEach { rule ->
                assertEquals(
                    "All rules should have the same time range start",
                    params.timeRangeStart,
                    rule.timeRangeStart
                )
                assertEquals(
                    "All rules should have the same time range end",
                    params.timeRangeEnd,
                    rule.timeRangeEnd
                )
                assertEquals(
                    "All rules should have the same total time",
                    params.totalTime,
                    rule.totalTime
                )
                assertEquals(
                    "All rules should have the same total count",
                    params.totalCount,
                    rule.totalCount
                )
                assertEquals(
                    "All rules should have the same app info ID",
                    params.appInfoId,
                    rule.appInfoId
                )
            }
        }
    }
    
    @Test
    fun propertyTest_CustomPatternRulesAreDistinct(): Unit = runBlocking {
        checkAll(100, customPatternParametersArb()) { params ->
            // Expand custom pattern
            val rules = RulePatternExpander.expandPattern(
                pattern = DayPattern.CUSTOM,
                selectedDays = params.selectedDays,
                timeRangeStart = params.timeRangeStart,
                timeRangeEnd = params.timeRangeEnd,
                totalTime = params.totalTime,
                totalCount = params.totalCount,
                appInfoId = params.appInfoId
            )
            
            // Verify each day appears exactly once
            val days = rules.map { it.day }
            assertEquals(
                "Each day should appear exactly once",
                days.size,
                days.toSet().size
            )
        }
    }
    
    @Test
    fun propertyTest_CustomPatternWithSingleDay(): Unit = runBlocking {
        checkAll(100, customPatternParametersArb()) { params ->
            // Test with just one day selected
            val singleDay = params.selectedDays.first()
            
            val rules = RulePatternExpander.expandPattern(
                pattern = DayPattern.CUSTOM,
                selectedDays = setOf(singleDay),
                timeRangeStart = params.timeRangeStart,
                timeRangeEnd = params.timeRangeEnd,
                totalTime = params.totalTime,
                totalCount = params.totalCount,
                appInfoId = params.appInfoId
            )
            
            // Verify exactly one rule is created
            assertEquals(
                "Custom pattern with one day should create exactly one rule",
                1,
                rules.size
            )
            
            // Verify it's for the correct day
            assertEquals(
                "Rule should be for the selected day",
                singleDay,
                rules.first().day
            )
        }
    }
}
