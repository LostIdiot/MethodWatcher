package cn.zz.xposed.methodwatcher.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cn.zz.xposed.methodwatcher.data.UiData
import cn.zz.xposed.methodwatcher.data.repository.main.MainRepository
import cn.zz.xposed.methodwatcher.data.source.MethodCallRecordsExportResult
import cn.zz.xposed.methodwatcher.data.toUiData
import cn.zz.xposed.methodwatcher.db.MonitorDatabase
import cn.zz.xposed.methodwatcher.monitor.monitorManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * 正在监听的应用数量
     */
    val activeTargetCountData = monitorManager.activeCountState

    /**
     * 总共记录的调用次数
     */
    val totalMethodCallCount = MonitorDatabase.database.recordDao().observeRecordCount()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    private val repository = MainRepository(application)

    /**
     * 记录导出结果数据
     */
    val exportData: Flow<UiData<MethodCallRecordsExportResult>?>
        get() = _exportData
    private val _exportData = MutableStateFlow<UiData<MethodCallRecordsExportResult>?>(null)

    /**
     * 删除全部记录结果数据
     */
    val deleteAllData: Flow<UiData<Boolean>?>
        get() = _deleteAllData
    private val _deleteAllData = MutableStateFlow<UiData<Boolean>?>(null)

    fun exportAllRecords() {
        viewModelScope.launch {
            _exportData.update {
                UiData.Loading()
            }
            val result = repository.exportAllRecords().toUiData()
            _exportData.update {
                result
            }
        }
    }

    fun deleteAllRecords() {
        viewModelScope.launch {
            _deleteAllData.update { UiData.Loading() }
            val result = repository.deleteAllRecords().toUiData()
            _deleteAllData.update {
                result
            }
        }
    }
}