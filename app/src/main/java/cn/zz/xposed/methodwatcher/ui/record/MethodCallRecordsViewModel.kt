package cn.zz.xposed.methodwatcher.ui.record

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cn.zz.xposed.methodwatcher.data.UiData
import cn.zz.xposed.methodwatcher.data.repository.record.MethodCallRecordsQueryData
import cn.zz.xposed.methodwatcher.data.repository.record.MethodCallRecordsRepository
import cn.zz.xposed.methodwatcher.data.repository.record.MonitorMethodItemData
import cn.zz.xposed.methodwatcher.data.repository.record.TargetAppItemData
import cn.zz.xposed.methodwatcher.data.source.MethodCallRecordsExportResult
import cn.zz.xposed.methodwatcher.data.toUiData
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MethodCallRecordsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MethodCallRecordsRepository(application)

    /**
     * 有记录的目标应用
     */
    val targetAppsData: Flow<List<TargetAppItemData>>
        get() = _targetAppsData
    private val _targetAppsData = MutableStateFlow<List<TargetAppItemData>>(emptyList())

    /**
     * 监听方法
     */
    val methodsData: Flow<List<MonitorMethodItemData>>
        get() = _methodsData
    private val _methodsData = MutableStateFlow<List<MonitorMethodItemData>>(emptyList())

    /**
     * 调用记录
     */
    val recordsData: Flow<List<MethodCallRecordData>>
        get() = _recordsData
    private val _recordsData = MutableStateFlow<List<MethodCallRecordData>>(emptyList())

    /**
     * 记录数量
     */
    val recordsCountData: Flow<Int>
        get() = _recordsCountData
    private val _recordsCountData = MutableStateFlow(0)

    /**
     * 导出记录的文件和mime
     */
    val exportedFileData: Flow<UiData<MethodCallRecordsExportResult>?>
        get() = _exportedFileData
    private val _exportedFileData = MutableStateFlow<UiData<MethodCallRecordsExportResult>?>(null)

    init {
        refresh()
    }

    /**
     * 更改目标应用选中状态
     * @param target 要更改的目标应用
     */
    fun toggleTargetAppSelected(target: TargetAppItemData) {
        val newTarget = target.copy(selected = !target.selected)
        val list = _targetAppsData.value.toMutableList()
        list[list.indexOf(target)] = newTarget
        _targetAppsData.update {
            list
        }
        loadRecords()
    }

    /**
     * 更改监控方法选中状态
     */
    fun toggleMonitorMethod(method: MonitorMethodItemData) {
        val newMethod = method.copy(selected = !method.selected)
        val list = _methodsData.value.toMutableList()
        list[list.indexOf(method)] = newMethod
        _methodsData.update {
            list
        }
        loadRecords()
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        viewModelScope.launch {
            val targetAppsDeferred = async { repository.loadTargetApps() }
            val methodsDeferred = async { repository.loadMonitorMethods() }

            val targetApps = targetAppsDeferred.await()
            val methods = methodsDeferred.await()

            _targetAppsData.update {
                targetApps
            }
            _methodsData.update {
                methods
            }
            loadRecords()
        }
    }

    /**
     * 导出记录
     */
    fun exportRecords() {
        viewModelScope.launch {
            val result = repository.exportRecords(_recordsData.value.map { it.entity }).toUiData()
            _exportedFileData.update {
                result
            }
        }
    }

    /**
     * 加载调用记录
     */
    private fun loadRecords() {
        viewModelScope.launch {
            val query =
                MethodCallRecordsQueryData(
                    _targetAppsData.value.mapNotNull { if (it.selected) it.packageName else null },
                    _methodsData.value.mapNotNull { if (it.selected) it.method else null })
            val records = (repository.loadCallRecords(query) ?: emptyList()).map {
                MethodCallRecordData(
                    it,
                    _targetAppsData.value.find { target ->
                        it.targetPackageName == target.packageName
                    })
            }
            _recordsData.update {
                records
            }
            _recordsCountData.update {
                records.size
            }
        }
    }
}