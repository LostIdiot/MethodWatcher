package cn.zz.xposed.methodwatcher.db

import androidx.room.*
import cn.zz.xposed.methodwatcher.data.MonitorMethod

@TypeConverters(MonitorMethodConverters::class)
@Entity(tableName = "monitor_config")
data class MonitorConfigEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "enabled")
    val enabled: Boolean = true,

    @ColumnInfo(name = "package_name")
    val packageName: String = "",

    @ColumnInfo(name = "watched_methods")
    val watchedMethods: List<MonitorMethod> = MonitorMethod.allMethods,

    @ColumnInfo(name = "modified_time")
    val modifiedTime: Long = 0
)