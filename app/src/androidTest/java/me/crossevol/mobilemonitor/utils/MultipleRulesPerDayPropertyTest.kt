package me.crossevol.mobilemonitor.utils

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import me.crossevol.mobilemonitor.data.database.AppRestrictionDatabase
import me.crossevol.mobilemonitor.model.AppInfo
import me.crossevol.mobilemonitor.model.AppRule
import me.crossevol.mobilemonitor.model.DayOfWeek
import me.crossevol.mobilemonitor.repository.AppRestrictionRepositoryImpl
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalTime

/**
 * Feature: app-usage-restriction, Property 9: Multiple rules per day are allowed
 * Validates: Requirements 3.8
 * 
 * For any day of the week, the system should allow saving multiple rules with
 * different time ranges for that same day.
 */
@RunWith(AndroidJUnit4::class)
class MultipleRulesPerDayPropertyTest {
    
    private lateinit var database: AppRestrictionDatabase
    private lateinit var repository: AppRestrictionRepositoryImpl
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppRestrictionDatabase::class.java
        ).allowMainThreadQueries().build()
        
        repository = AppRestrictionRepositoryImpl(
            database.appInfoDao(),
            database.appRuleDao(),
            ApplicationProvider.getApplicationContext()
        )
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    // Generator for multiple rules on the same day
    private fun multipleRulesArb(appInfoId: Long) = arbitrary {
        val day = DayOfWeek.values()[Arb.int(0..6).bind()]
        val ruleCount = Arb.int(2..5).bind()
        
        MultipleRulesData(
            day = day,
            rules = (1..ruleCount).map {
                val startHour = Arb.int(0..23).bind()
                val startMinute = Arb.int(0..59).bind()
                val endHour = Arb.int(0..23).bind()
                val endMinute = Arb.int(0..59).bind()
                
                AppRule(
                    id = 0,
                    day = day,
                    timeRangeStart = LocalTime.of(startHour, startMinute),
                    timeRangeEnd = LocalTime.of(endHour, endMinute),
                    totalTime = Arb.int(0..1440).bind(),
                    totalCount = Arb.int(0..100).bind(),
                    appInfoId = appInfoId,
                    createdTime = Arb.long(1000000000000L..2000000000000L).bind()
                )
            }
        )
    }
    
    data class MultipleRulesData(
        val day: DayOfWeek,
        val rules: List<AppRule>
    )
    
    @Test
    fun propertyTest_MultipleRulesPerDayAreAllowed(): Unit = runBlocking {
        // Create an app to associate rules with
        val appId = repository.saveApp(
            AppInfo(
                appName = "Test App",
                packageName = "com.test.multiplerules",
                enabled = true
            )
        )
        
        checkAll(100, multipleRulesArb(appId)) { data ->
            // Save all rules for the same day
            val savedRuleIds = mutableListOf<Long>()
            data.rules.forEach { rule ->
                val ruleId = repository.saveRule(rule)
                savedRuleIds.add(ruleId)
            }
            
            // Verify all rules were saved successfully
            assertEquals(
                "All rules should be saved",
                data.rules.size,
                savedRuleIds.size
            )
            
            assertTrue(
                "All rule IDs should be positive",
                savedRuleIds.all { it > 0 }
            )
            
            // Retrieve all rules for the app
            val retrievedRules = mutableListOf<AppRule>()
            savedRuleIds.forEach { ruleId ->
                val rule = repository.getRuleById(ruleId)
                rule?.let { retrievedRules.add(it) }
            }
            
            // Verify all rules were retrieved
            assertEquals(
                "All rules should be retrievable",
                data.rules.size,
                retrievedRules.size
            )
            
            // Verify all rules are for the same day
            assertTrue(
                "All rules should be for the same day",
                retrievedRules.all { it.day == data.day }
            )
            
            // Verify rules can have different time ranges
            // (This is implicitly tested by saving multiple rules with potentially different times)
            
            // Clean up
            savedRuleIds.forEach { ruleId ->
                repository.deleteRule(ruleId)
            }
        }
        
        // Clean up app
        repository.deleteApp(appId)
    }
    
    @Test
    fun propertyTest_MultipleRulesPerDayWithDifferentTimeRanges(): Unit = runBlocking {
        // Create an app
        val appId = repository.saveApp(
            AppInfo(
                appName = "Test App 2",
                packageName = "com.test.multiplerules2",
                enabled = true
            )
        )
        
        checkAll(100, multipleRulesArb(appId)) { data ->
            // Create rules with explicitly different time ranges
            val rule1 = AppRule(
                id = 0,
                day = data.day,
                timeRangeStart = LocalTime.of(9, 0),
                timeRangeEnd = LocalTime.of(12, 0),
                totalTime = 60,
                totalCount = 5,
                appInfoId = appId
            )
            
            val rule2 = AppRule(
                id = 0,
                day = data.day,
                timeRangeStart = LocalTime.of(14, 0),
                timeRangeEnd = LocalTime.of(17, 0),
                totalTime = 90,
                totalCount = 10,
                appInfoId = appId
            )
            
            // Save both rules
            val ruleId1 = repository.saveRule(rule1)
            val ruleId2 = repository.saveRule(rule2)
            
            // Verify both were saved
            assertTrue("First rule should be saved", ruleId1 > 0)
            assertTrue("Second rule should be saved", ruleId2 > 0)
            
            // Retrieve both rules
            val retrieved1 = repository.getRuleById(ruleId1)
            val retrieved2 = repository.getRuleById(ruleId2)
            
            // Verify both exist
            assertTrue("First rule should be retrievable", retrieved1 != null)
            assertTrue("Second rule should be retrievable", retrieved2 != null)
            
            // Verify they're for the same day but different time ranges
            assertEquals("Both rules should be for the same day", retrieved1?.day, retrieved2?.day)
            assertTrue(
                "Rules should have different time ranges",
                retrieved1?.timeRangeStart != retrieved2?.timeRangeStart ||
                retrieved1?.timeRangeEnd != retrieved2?.timeRangeEnd
            )
            
            // Clean up
            repository.deleteRule(ruleId1)
            repository.deleteRule(ruleId2)
        }
        
        // Clean up app
        repository.deleteApp(appId)
    }
}
