package me.crossevol.mobilemonitor.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_rule",
    foreignKeys = [
        ForeignKey(
            entity = AppInfoEntity::class,
            parentColumns = ["id"],
            childColumns = ["app_info_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("app_info_id")]
)
data class AppRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "day")
    val day: Int, // 1-7 for Monday-Sunday
    
    @ColumnInfo(name = "time_range_start")
    val timeRangeStart: String, // HH:mm format
    
    @ColumnInfo(name = "time_range_end")
    val timeRangeEnd: String, // HH:mm format
    
    @ColumnInfo(name = "total_time")
    val totalTime: Int, // minutes
    
    @ColumnInfo(name = "total_count")
    val totalCount: Int,
    
    @ColumnInfo(name = "created_time")
    val createdTime: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "app_info_id")
    val appInfoId: Long
)
