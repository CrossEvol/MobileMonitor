package me.crossevol.mobilemonitor.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.crossevol.mobilemonitor.data.entity.AppInfoEntity

@Dao
interface AppInfoDao {
    @Query("SELECT * FROM app_info")
    fun getAllApps(): Flow<List<AppInfoEntity>>
    
    @Query("SELECT * FROM app_info WHERE id = :appId")
    suspend fun getAppById(appId: Long): AppInfoEntity?
    
    @Query("SELECT * FROM app_info WHERE package_name = :packageName")
    suspend fun getAppByPackageName(packageName: String): AppInfoEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppInfoEntity): Long
    
    @Update
    suspend fun updateApp(app: AppInfoEntity)
    
    @Delete
    suspend fun deleteApp(app: AppInfoEntity)
}
