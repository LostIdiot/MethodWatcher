package cn.zz.xposed.methodwatcher.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cn.zz.xposed.methodwatcher.app

@Database(
    entities = [MonitorConfigEntity::class, MonitorRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MonitorDatabase : RoomDatabase() {

    abstract fun configDao(): MonitorConfigDao

    abstract fun recordDao(): MonitorRecordDao

    companion object {

        private const val NAME = "system_call_monitor"

        val database: MonitorDatabase = Room.databaseBuilder(
            app,
            MonitorDatabase::class.java,
            NAME
        ).build()
    }
}