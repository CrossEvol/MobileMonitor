package me.crossevol.mobilemonitor.data.database

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
import me.crossevol.mobilemonitor.data.dao.AppInfoDao
import me.crossevol.mobilemonitor.data.dao.AppRuleDao
import me.crossevol.mobilemonitor.data.entity.AppInfoEntity
import me.crossevol.mobilemonitor.data.entity.AppRuleEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Feature: app-usage-restriction, Property 10: Database persistence round-trip
 * Validates: Requirements 4.2, 4.4, 4.6
 * 
 * For any app info or rule, after saving to the database and retrieving it,
 * all fields should have identical values to the original.
 */
@RunWith(AndroidJUnit4::class)
class DatabasePersistencePropertyTest {
    
    private lateinit var database: AppRestrictionDatabase
    private lateinit var appInfoDao: AppInfoDao
    private lateinit var appRuleDao: AppRuleDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppRestrictionDatabase::class.java
        ).allowMainThreadQueries().build()
        
        appInfoDao = database.appInfoDao()
        appRuleDao = database.appRuleDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    // Generator for AppInfoEntity
    private fun appInfoArb() = arbitrary {
        AppInfoEntity(
            id = 0, // Will be auto-generated
            appName = Arb.string(1..50).bind(),
            packageName = "com.example." + Arb.string(1..20).bind(),
            enabled = Arb.boolean().bind(),
            createdTime = Arb.long(1000000000000L..2000000000000L).bind()
        )
    }
    
    // Generator for AppRuleEntity
    private fun appRuleArb(appInfoId: Long) = arbitrary {
        val startHour = Arb.int(0..23).bind()
        val startMinute = Arb.int(0..59).bind()
        val endHour = Arb.int(0..23).bind()
        val endMinute = Arb.int(0..59).bind()
        
        AppRuleEntity(
            id = 0, // Will be auto-generated
            day = Arb.int(1..7).bind(),
            timeRangeStart = String.format("%02d:%02d", startHour, startMinute),
            timeRangeEnd = String.format("%02d:%02d", endHour, endMinute),
            totalTime = Arb.int(0..1440).bind(), // 0 to 24 hours in minutes
            totalCount = Arb.int(0..100).bind(),
            createdTime = Arb.long(1000000000000L..2000000000000L).bind(),
            appInfoId = appInfoId
        )
    }
    
    @Test
    fun propertyTest_AppInfoPersistenceRoundTrip() = runBlocking {
        checkAll(100, appInfoArb()) { appInfo ->
            // Save to database
            val insertedId = appInfoDao.insertApp(appInfo)
            
            // Retrieve from database
            val retrieved = appInfoDao.getAppById(insertedId)
            
            // Verify all fields match
            assertNotNull(retrieved)
            assertEquals(appInfo.appName, retrieved!!.appName)
            assertEquals(appInfo.packageName, retrieved.packageName)
            assertEquals(appInfo.enabled, retrieved.enabled)
            assertEquals(appInfo.createdTime, retrieved.createdTime)
            
            // Clean up for next iteration
            appInfoDao.deleteApp(retrieved)
        }
    }
    
    @Test
    fun propertyTest_AppRulePersistenceRoundTrip() = runBlocking {
        checkAll(100, appInfoArb()) { appInfo ->
            // First insert an app to get a valid appInfoId
            val appId = appInfoDao.insertApp(appInfo)
            
            // Generate and test a rule for this app
            checkAll(1, appRuleArb(appId)) { appRule ->
                // Save rule to database
                val ruleId = appRuleDao.insertRule(appRule)
                
                // Retrieve from database
                val retrieved = appRuleDao.getRuleById(ruleId)
                
                // Verify all fields match
                assertNotNull(retrieved)
                assertEquals(appRule.day, retrieved!!.day)
                assertEquals(appRule.timeRangeStart, retrieved.timeRangeStart)
                assertEquals(appRule.timeRangeEnd, retrieved.timeRangeEnd)
                assertEquals(appRule.totalTime, retrieved.totalTime)
                assertEquals(appRule.totalCount, retrieved.totalCount)
                assertEquals(appRule.createdTime, retrieved.createdTime)
                assertEquals(appRule.appInfoId, retrieved.appInfoId)
            }
            
            // Clean up
            val app = appInfoDao.getAppById(appId)
            if (app != null) {
                appInfoDao.deleteApp(app)
            }
        }
    }
}
