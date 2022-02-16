package cn.zz.xposed.methodwatcher.data.repository.target

import cn.zz.xposed.methodwatcher.data.repository.Repository
import cn.zz.xposed.methodwatcher.db.MonitorConfigEntity
import cn.zz.xposed.methodwatcher.db.MonitorDatabase
import kotlinx.coroutines.flow.Flow

class MonitorTargetRepository(private val packageName: String) : Repository {

    private val config by lazy {
        MonitorDatabase.database.configDao().observeConfig(packageName)
    }

    fun observeConfig(): Flow<MonitorConfigEntity?> {
        return config
    }

    suspend fun updateConfig(config: MonitorConfigEntity) {
        MonitorDatabase.database.configDao().updateConfig(config)
    }

    fun observeRecordsCount() =
        MonitorDatabase.database.recordDao().observeRecordCountByPackageName(packageName)

    suspend fun deleteRecords() {
        MonitorDatabase.database.recordDao().deleteRecords(packageName)
    }
}