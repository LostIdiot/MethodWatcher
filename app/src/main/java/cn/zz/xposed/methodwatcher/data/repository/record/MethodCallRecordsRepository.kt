package cn.zz.xposed.methodwatcher.data.repository.record

import android.app.Application
import cn.zz.xposed.methodwatcher.data.DataResult
import cn.zz.xposed.methodwatcher.data.MonitorMethod
import cn.zz.xposed.methodwatcher.data.repository.Repository
import cn.zz.xposed.methodwatcher.data.source.MethodCallRecordsExportResult
import cn.zz.xposed.methodwatcher.data.source.MethodCallRecordsExportSource
import cn.zz.xposed.methodwatcher.db.MonitorDatabase
import cn.zz.xposed.methodwatcher.db.MonitorRecordEntity

class MethodCallRecordsRepository(private val application: Application) : Repository {

    private val exportRecordsSource by lazy {
        MethodCallRecordsExportSource(application)
    }

    /**
     * 加载目标应用
     */
    suspend fun loadTargetApps(): List<TargetAppItemData> {
        val packageNames = MonitorDatabase.database.recordDao().loadAllPackageNames()
        val list = mutableListOf<TargetAppItemData>()
        if (!packageNames.isNullOrEmpty()) {
            if (!packageNames.isNullOrEmpty()) {
                val packageManager = application.packageManager
                for (packageName in packageNames) {
                    val applicationInfo = kotlin.runCatching {
                        packageManager.getApplicationInfo(packageName, 0)
                    }.getOrNull()
                    if (applicationInfo != null) {
                        val label = packageManager.getApplicationLabel(applicationInfo)
                        list.add(TargetAppItemData(label.toString(), packageName))
                    } else {
                        list.add(TargetAppItemData("", packageName))
                    }
                }
            }
        }
        return list
    }

    /**
     * 加载监控方法
     */
    suspend fun loadMonitorMethods() = MonitorMethod.allMethods.map { MonitorMethodItemData(it) }


    /**
     * 加载调用记录
     */
    suspend fun loadCallRecords(query: MethodCallRecordsQueryData) =
        MonitorDatabase.database.recordDao().loadRecords(query.targetPackageNames, query.methods)

    /**
     * 导出调用记录
     */
    suspend fun exportRecords(records: List<MonitorRecordEntity>): DataResult<MethodCallRecordsExportResult> {
        return exportRecordsSource.exportRecords(records)
    }
}