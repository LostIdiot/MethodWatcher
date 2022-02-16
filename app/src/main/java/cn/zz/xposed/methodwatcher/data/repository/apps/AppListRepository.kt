package cn.zz.xposed.methodwatcher.data.repository.apps

import android.app.Application
import cn.zz.xposed.methodwatcher.db.MonitorDatabase
import kotlinx.coroutines.flow.flow

class AppListRepository(private val application: Application) {

    /**
     * 加载app列表
     */
    fun loadAppList() = flow {
        val appList = application.packageManager.getInstalledPackages(0)
            .filter { it.packageName != application.packageName }
        emit(appList)
    }

    /**
     * 监听所有app的配置数据
     */
    fun observeAllConfigs() = MonitorDatabase.database.configDao().observeAllConfig()
}