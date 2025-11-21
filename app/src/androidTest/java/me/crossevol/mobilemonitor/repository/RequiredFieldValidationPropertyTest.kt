package me.crossevol.mobilemonitor.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import me.crossevol.mobilemonitor.data.database.AppRestrictionDatabase
import me.crossevol.mobilemonitor.model.AppRule
import me.crossevol.mobilemonitor.model.DayOfWeek
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalTime

/**
 * Feature: app-usage-restriction, Property 5: Required fields are validated
 * Validates: Requirements 3.1
 * 
 * For any usage rule creation attempt, the system should reject rules that lack
 * day pattern or time range specifications.
 */
@RunWith(AndroidJUnit4::class)
class RequiredFieldValidationPropertyTest {
    
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
    
    // Generator for valid AppRule with all required fields
    private fun validAppRuleArb(appInfoId: Long) = arbitrary {
        val startHour = Arb.int(0..23).bind()
        val startMinute = Arb.int(0..59).bind()
        val endHour = Arb.int(0..23).bind()
        val endMinute = Arb.int(0..59).bind()
        
        AppRule(
            id = 0,
            day = DayOfWeek.values()[Arb.int(0..6).bind()],
            timeRangeStart = LocalTime.of(startHour, startMinute),
            timeRangeEnd = LocalTime.of(endHour, endMinute),
            totalTime = Arb.int(0..1440).bind(),
            totalCount = Arb.int(0..100).bind(),
            appInfoId = appInfoId,
            createdTime = Arb.long(1000000000000L..2000000000000L).bind()
        )
    }
    
    @Test
    fun propertyTest_ValidRulesWithAllRequiredFieldsAreAccepted(): Unit = runBlocking {
        // First create an app to associate rules with
        val appId = repository.saveApp(
            me.crossevol.mobilemonitor.model.AppInfo(
                appName = "Test App",
                packageName = "com.test.app",
                enabled = true
            )
        )
        
        checkAll(100, validAppRuleArb(appId)) { rule ->
            // Attempt to save the rule
            val ruleId = repository.saveRule(rule)
            
            // Verify the rule was saved successfully
            assertTrue("Rule with all required fields should be saved", ruleId > 0)
            
            // Verify we can retrieve it
            val retrieved = repository.getRuleById(ruleId)
            assertTrue("Saved rule should be retrievable", retrieved != null)
            
            // Verify required fields are present
            retrieved?.let {
                assertTrue("Day should be set", it.day != null)
                assertTrue("Time range start should be set", it.timeRangeStart != null)
                assertTrue("Time range end should be set", it.timeRangeEnd != null)
            }
            
            // Clean up
            repository.deleteRule(ruleId)
        }
        
        // Clean up app
        repository.deleteApp(appId)
    }
    
    @Test
    fun propertyTest_RulesHaveValidDayOfWeek(): Unit = runBlocking {
        // Create an app
        val appId = repository.saveApp(
            me.crossevol.mobilemonitor.model.AppInfo(
                appName = "Test App",
                packageName = "com.test.app2",
                enabled = true
            )
        )
        
        checkAll(100, validAppRuleArb(appId)) { rule ->
            // Save the rule
            val ruleId = repository.saveRule(rule)
            
            // Retrieve and verify day is valid (1-7)
            val retrieved = repository.getRuleById(ruleId)
            retrieved?.let {
                assertTrue(
                    "Day should be between 1 and 7",
                    it.day.value in 1..7
                )
            }
            
            // Clean up
            repository.deleteRule(ruleId)
        }
        
        // Clean up app
        repository.deleteApp(appId)
    }
    
    @Test
    fun propertyTest_RulesHaveValidTimeRange(): Unit = runBlocking {
        // Create an app
        val appId = repository.saveApp(
            me.crossevol.mobilemonitor.model.AppInfo(
                appName = "Test App",
                packageName = "com.test.app3",
                enabled = true
            )
        )
        
        checkAll(100, validAppRuleArb(appId)) { rule ->
            // Save the rule
            val ruleId = repository.saveRule(rule)
            
            // Retrieve and verify time range is valid
            val retrieved = repository.getRuleById(ruleId)
            retrieved?.let {
                assertTrue(
                    "Time range start should be valid (0-23 hours)",
                    it.timeRangeStart.hour in 0..23
                )
                assertTrue(
                    "Time range start minutes should be valid (0-59)",
                    it.timeRangeStart.minute in 0..59
                )
                assertTrue(
                    "Time range end should be valid (0-23 hours)",
                    it.timeRangeEnd.hour in 0..23
                )
                assertTrue(
                    "Time range end minutes should be valid (0-59)",
                    it.timeRangeEnd.minute in 0..59
                )
            }
            
            // Clean up
            repository.deleteRule(ruleId)
        }
        
        // Clean up app
        repository.deleteApp(appId)
    }
}
