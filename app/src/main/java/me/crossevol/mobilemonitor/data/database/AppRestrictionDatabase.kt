package me.crossevol.mobilemonitor.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import me.crossevol.mobilemonitor.data.dao.AppInfoDao
import me.crossevol.mobilemonitor.data.dao.AppRuleDao
import me.crossevol.mobilemonitor.data.entity.AppInfoEntity
import me.crossevol.mobilemonitor.data.entity.AppRuleEntity

@Database(
    entities = [AppInfoEntity::class, AppRuleEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppRestrictionDatabase : RoomDatabase() {
    abstract fun appInfoDao(): AppInfoDao
    abstract fun appRuleDao(): AppRuleDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppRestrictionDatabase? = null
        
        fun getDatabase(context: Context): AppRestrictionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRestrictionDatabase::class.java,
                    "app_restriction_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
