package cn.zz.xposed.methodwatcher.ui.apps

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.zz.xposed.methodwatcher.data.repository.apps.AppListItemData
import cn.zz.xposed.methodwatcher.data.repository.apps.AppListRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AppListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppListRepository(application)

    val appListData: LiveData<List<AppListItemData>>
        get() = _appListData
    private val _appListData = MutableLiveData<List<AppListItemData>>()
    private val appList = mutableListOf<AppListItemData>()

    val refreshingData: LiveData<Boolean>
        get() = _refreshingData
    private val _refreshingData = MutableLiveData<Boolean>()

    val showSystemAppData: LiveData<Boolean>
        get() = _showSystemAppData
    private val _showSystemAppData = MutableLiveData(false)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshingData.value = true
            combine(repository.loadAppList(), repository.observeAllConfigs()) { appList, configs ->
                val result = mutableListOf<AppListItemData>()
                for (info in appList) {
                    result.add(
                        AppListItemData(
                            info,
                            configs?.find { it.packageName == info.packageName })
                    )
                }
                result
            }.collect {
                appList.clear()
                appList.addAll(it)
                notifyAppList()
                _refreshingData.value = false
            }
        }
    }

    fun toggleShowSystemApps() {
        _showSystemAppData.value = !(showSystemAppData.value ?: false)
        notifyAppList()
    }

    private fun notifyAppList() {
        _appListData.value =
            if (showSystemAppData.value == true) {
                appList
            } else {
                appList.filter { it.packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            }
    }
}