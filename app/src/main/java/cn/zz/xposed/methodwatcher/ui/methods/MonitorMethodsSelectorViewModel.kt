package cn.zz.xposed.methodwatcher.ui.methods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.zz.xposed.methodwatcher.data.MonitorMethod
import cn.zz.xposed.methodwatcher.data.MonitorMethodGroup
import cn.zz.xposed.methodwatcher.data.UiData
import cn.zz.xposed.methodwatcher.data.repository.methods.MonitorMethodItemData
import cn.zz.xposed.methodwatcher.data.repository.methods.MonitorMethodsSelectorRepository
import cn.zz.xposed.methodwatcher.data.toUiData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * [ViewModel] for [MonitorMethodsSelectorActivity]
 */
class MonitorMethodsSelectorViewModel(monitoringMethods: List<MonitorMethod>) :
    ViewModel() {

    private val repository = MonitorMethodsSelectorRepository(monitoringMethods)

    /**
     * All monitor-able method groups
     *
     * 所有可被监听的方法分组
     */
    val methodGroupsData: Flow<UiData<List<MonitorMethodGroup>>>
        get() = _methodGroupsData
    private val _methodGroupsData = MutableStateFlow<UiData<List<MonitorMethodGroup>>>(UiData.Empty)

    /**
     * 选择的要监听的方法
     */
    val selectedMethodsData: Flow<UiData<List<MonitorMethod>>>
        get() = _selectedMethodsData
    private val _selectedMethodsData = MutableStateFlow<UiData<List<MonitorMethod>>>(UiData.Empty)

    init {
        viewModelScope.launch {
            val groups = repository.loadGroups().toUiData()
            _methodGroupsData.value = groups
        }
    }

    /**
     * 监听指定分组的方法
     */
    fun observeMethodsOfGroup(group: MonitorMethodGroup): Flow<List<MonitorMethodItemData>> {
        return repository.observeMethodsOfGroup(group)
    }

    /**
     * 切换方法选中状态
     */
    fun toggleMethodSelected(method: MonitorMethodItemData) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.toggleMethodSelected(method)
        }
    }

    /**
     * 重置选择状态为初始值
     */
    fun reset() {
        viewModelScope.launch(Dispatchers.Default) {
            repository.reset()
        }
    }

    /**
     * 保存选择的方法
     */
    fun select() {
        viewModelScope.launch {
            val result = repository.select().toUiData()
            _selectedMethodsData.update {
                result
            }
        }
    }
}