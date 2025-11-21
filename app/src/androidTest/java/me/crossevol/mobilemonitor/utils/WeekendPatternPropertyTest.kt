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
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalTime

/**
 * Feature: app-usage-restriction, Property 7: Weekend pattern creates two rules
 * Validates: Requirements 3.6
 * 
 * For any time range and restriction values, selecting the weekend pattern should
 * create exactly two rules with identical settings for Saturday and Sunday.
 */
@RunWith(AndroidJUnit4::class)
class WeekendPatternPropertyTest {
    
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
    fun propertyTest_WeekendPatternCreatesTwoRules(): Unit = runBlocking {
        checkAll(100, ruleParametersArb()) { params ->
            // Expand weekend pattern
            val rules = RulePatternExpander.expandPattern(
                pattern = DayPattern.WEEKEND,
                selectedDays = emptySet(), // Not used for WEEKEND
                timeRangeStart = params.timeRangeStart,
                timeRangeEnd = params.timeRangeEnd,
                totalTime = params.totalTime,
                totalCount = params.totalCount,
                appInfoId = params.appInfoId
            )
            
            // Verify exactly 2 rules are created
            assertEquals(
                "Weekend pattern should create exactly 2 rules",
                2,
                rules.size
            )
            
            // Verify rules are for Saturday and Sunday
            val expectedDays = setOf(
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
            )
            val actualDays = rules.map { it.day }.toSet()
            assertEquals(
                "Weekend pattern should create rules for Saturday and Sunday",
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
    fun propertyTest_WeekendPatternRulesAreDistinct(): Unit = runBlocking {
        checkAll(100, ruleParametersArb()) { params ->
            // Expand weekend pattern
            val rules = RulePatternExpander.expandPattern(
                pattern = DayPattern.WEEKEND,
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
