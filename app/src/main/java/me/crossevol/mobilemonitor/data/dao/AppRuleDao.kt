package me.crossevol.mobilemonitor.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.crossevol.mobilemonitor.data.entity.AppRuleEntity

@Dao
interface AppRuleDao {
    @Query("SELECT * FROM app_rule WHERE app_info_id = :appId")
    fun getRulesForApp(appId: Long): Flow<List<AppRuleEntity>>
    
    @Query("SELECT * FROM app_rule WHERE id = :ruleId")
    suspend fun getRuleById(ruleId: Long): AppRuleEntity?
    
    @Query("""
        SELECT ar.* FROM app_rule ar
        INNER JOIN app_info ai ON ar.app_info_id = ai.id
        WHERE ai.enabled = 1
    """)
    suspend fun getAllEnabledRules(): List<AppRuleEntity>
    
    @Insert
    suspend fun insertRule(rule: AppRuleEntity): Long
    
    @Insert
    suspend fun insertRules(rules: List<AppRuleEntity>)
    
    @Update
    suspend fun updateRule(rule: AppRuleEntity)
    
    @Delete
    suspend fun deleteRule(rule: AppRuleEntity)
    
    @Query("DELETE FROM app_rule WHERE app_info_id = :appId")
    suspend fun deleteAllRulesByAppId(appId: Long)
}
