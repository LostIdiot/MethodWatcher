package cn.zz.xposed.methodwatcher.db

import androidx.room.TypeConverter
import cn.zz.xposed.methodwatcher.data.MonitorMethod

class MonitorMethodConverters {

    @TypeConverter
    fun monitorMethodsToString(list: List<MonitorMethod>): String {
        return list.joinToString(",") {
            it.id.toString()
        }
    }

    @TypeConverter
    fun stringToMonitorMethods(value: String): List<MonitorMethod> {
        return value.split(',').mapNotNull {
            MonitorMethod.findById(it.toIntOrNull() ?: MonitorMethod.Unknown.id)
                .takeIf { method -> method != MonitorMethod.Unknown }
        }
    }

    @TypeConverter
    fun monitorMethodToInt(method: MonitorMethod): Int = method.id

    @TypeConverter
    fun intToMonitorMethod(value: Int): MonitorMethod = MonitorMethod.findById(value)
}