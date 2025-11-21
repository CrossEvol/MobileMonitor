package me.crossevol.mobilemonitor.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
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
 * Feature: app-usage-restriction, Property 6: Workday pattern creates five rules
 * Validates: Requirements 3.5
 * 
 * For any time range and restriction values, selecting the workday pattern should
 * create exactly five rules with identical settings for Monday through Friday.
 */
@RunWith(AndroidJUnit4::class)
class WorkdayPatternPropertyTest {
    
    // Generator for time ranges and restriction values
    private fun ruleParametersArb() = arbitrary {
        val startHour = Arb.int(0..23).bind()
        val startMinute = Arb.int(0..59).bind()
        val endHour = Arb.int(0..23).bind()
        val endMinute = Arb.int(0..59).bind()
        
        RuleParameters(
            timeRangeStart = LocalTime.of(startHour, startMinute),
            timeRangeEnd = LocalTime.of(endHour, endMinute),
            totalTime = Arb.int(0..1440).bind(),
            totalCount = Arb.int(0..100).bind(),
            appInfoId = Arb.long(1L..1000L).bind()
        )
    }
    
    data class RuleParameters(
        val timeRangeStart: LocalTime,
        val timeRangeEnd: LocalTime,
        val totalTime: Int,
        val totalCount: Int,
        val appInfoId: Long
    )
    
    @Test
    fun propertyTest_WorkdayPatternCreatesFiveRules(): Unit = runBlocking {
        checkAll(100, ruleParametersArb()) { params ->
            // Expand workday pattern
            val rules = RulePatternExpander.expandPattern(
                pattern = DayPattern.WORKDAY,
                selectedDays = emptySet(), // Not used for WORKDAY
                timeRangeStart = params.timeRangeStart,
                timeRangeEnd = params.timeRangeEnd,
                totalTime = params.totalTime,
                totalCount = params.totalCount,
                appInfoId = params.appInfoId
            )
            
            // Verify exactly 5 rules are created
            assertEquals(
                "Workday pattern should create exactly 5 rules",
                5,
                rules.size
            )
            
            // Verify rules are for Monday through Friday
            val expectedDays = setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            )
            val actualDays = rules.map { it.day }.toSet()
            assertEquals(
                "Workday pattern should create rules for Monday through Friday",
                expectedDays,
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
    fun propertyTest_WorkdayPatternRulesAreDistinct(): Unit = runBlocking {
        checkAll(100, ruleParametersArb()) { params ->
            // Expand workday pattern
            val rules = RulePatternExpander.expandPattern(
                pattern = DayPattern.WORKDAY,
                selectedDays = emptySet(),
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
}
