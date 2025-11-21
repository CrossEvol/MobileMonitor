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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Feature: app-usage-restriction, Property 11: Referential integrity is maintained
 * Validates: Requirements 4.5
 * 
 * For any app deletion, all associated rules should be automatically deleted,
 * and no orphaned rules should exist in the database.
 */
@RunWith(AndroidJUnit4::class)
class ReferentialIntegrityPropertyTest {
    
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
            id = 0,
            appName = Arb.string(1..50).bind(),
            packageName = "com.example." + Arb.string(1..20).bind(),
            enabled = Arb.boolean().bind(),
            createdTime = Arb.long(1000000000000L..2000000000000L).bind()
        )
    }
    
    // Generator for list of AppRuleEntity
    private fun appRulesArb(appInfoId: Long) = arbitrary {
        val count = Arb.int(1..10).bind()
        List(count) {
            val startHour = Arb.int(0..23).bind()
            val startMinute = Arb.int(0..59).bind()
            val endHour = Arb.int(0..23).bind()
            val endMinute = Arb.int(0..59).bind()
            
            AppRuleEntity(
                id = 0,
                day = Arb.int(1..7).bind(),
                timeRangeStart = String.format("%02d:%02d", startHour, startMinute),
                timeRangeEnd = String.format("%02d:%02d", endHour, endMinute),
                totalTime = Arb.int(0..1440).bind(),
                totalCount = Arb.int(0..100).bind(),
                createdTime = Arb.long(1000000000000L..2000000000000L).bind(),
                appInfoId = appInfoId
            )
        }
    }
    
    @Test
    fun propertyTest_DeletingAppCascadesToDeleteAllRules() = runBlocking {
        checkAll(100, appInfoArb()) { appInfo ->
            // Insert app
            val appId = appInfoDao.insertApp(appInfo)
            
            // Generate and insert rules for this app
            checkAll(1, appRulesArb(appId)) { rules ->
                val insertedRuleIds = mutableListOf<Long>()
                for (rule in rules) {
                    val ruleId = appRuleDao.insertRule(rule)
                    insertedRuleIds.add(ruleId)
                }
                
                // Delete the app
                val app = appInfoDao.getAppById(appId)
                if (app != null) {
                    appInfoDao.deleteApp(app)
                }
                
                // Verify all rules are deleted (cascade delete)
                for (ruleId in insertedRuleIds) {
                    val retrievedRule = appRuleDao.getRuleById(ruleId)
                    assertNull("Rule $ruleId should be deleted when app is deleted", retrievedRule)
                }
                
                // Verify app is deleted
                val retrievedApp = appInfoDao.getAppById(appId)
                assertNull("App should be deleted", retrievedApp)
            }
        }
    }
    
    @Test
    fun propertyTest_NoOrphanedRulesExistAfterAppDeletion() = runBlocking {
        checkAll(50, appInfoArb()) { appInfo ->
            // Insert app
            val appId = appInfoDao.insertApp(appInfo)
            
            // Generate and insert multiple rules
            checkAll(1, appRulesArb(appId)) { rules ->
                val insertedRuleIds = mutableListOf<Long>()
                for (rule in rules) {
                    val ruleId = appRuleDao.insertRule(rule)
                    insertedRuleIds.add(ruleId)
                }
                
                // Delete the app
                val app = appInfoDao.getAppById(appId)
                if (app != null) {
                    appInfoDao.deleteApp(app)
                }
                
                // Verify no rules remain for this app
                var orphanedRuleCount = 0
                for (ruleId in insertedRuleIds) {
                    val rule = appRuleDao.getRuleById(ruleId)
                    if (rule != null) {
                        orphanedRuleCount++
                    }
                }
                
                assertEquals(
                    "No orphaned rules should exist after app deletion",
                    0,
                    orphanedRuleCount
                )
            }
        }
    }
}
