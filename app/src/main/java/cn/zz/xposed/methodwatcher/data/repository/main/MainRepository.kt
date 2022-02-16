package cn.zz.xposed.methodwatcher.data.repository.main

import android.app.Application
import cn.zz.xposed.methodwatcher.data.DataException
import cn.zz.xposed.methodwatcher.data.DataResult
import cn.zz.xposed.methodwatcher.data.repository.Repository
import cn.zz.xposed.methodwatcher.data.source.MethodCallRecordsExportResult
import cn.zz.xposed.methodwatcher.data.source.MethodCallRecordsExportSource
import cn.zz.xposed.methodwatcher.db.MonitorDatabase

class MainRepository(application: Application) : Repository {

    private val exportRecordsSource by lazy {
        MethodCallRecordsExportSource(application)
    }

    /**
     * 导出全部调用记录
     */
    suspend fun exportAllRecords(): DataResult<MethodCallRecordsExportResult> {
        val records = MonitorDatabase.database.recordDao().loadAllRecords() ?: emptyList()
        return if (records.isEmpty()) {
            DataResult.Error(DataException(message = "暂无记录可以导出"))
        } else {
            exportRecordsSource.exportRecords(records)
        }
    }

    /**
     * 删除全部调用记录
     */
    suspend fun deleteAllRecords(): DataResult<Boolean> {
        MonitorDatabase.database.recordDao().deleteAllRecords()
        return DataResult.Success(true)
    }
}