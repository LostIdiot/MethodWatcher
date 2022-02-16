package cn.zz.xposed.methodwatcher.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import cn.zz.xposed.methodwatcher.data.MonitorMethod
import cn.zz.xposed.methodwatcher.db.MonitorMethodConverters

@TypeConverters(MonitorMethodConverters::class)
@Entity(tableName = "monitor_record")
data class MonitorRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 方法调用发生的应用包名
     */
    @ColumnInfo(name = "target_package_name")
    val targetPackageName: String,

    /**
     * 方法调用发生的应用进程名
     */
    @ColumnInfo(name = "target_process_name")
    val targetProcessName: String,

    /**
     * 调用的方法
     */
    @ColumnInfo(name = "method")
    val method: MonitorMethod,

    /**
     * 调用栈
     */
    @ColumnInfo(name = "call_trace")
    val callTrace: String,

    /**
     * 直接调用者
     */
    @ColumnInfo(name = "direct_caller")
    val directCaller: String,

    /**
     * 调用发生的时间
     */
    @ColumnInfo(name = "called_time")
    val calledTime: Long,
)