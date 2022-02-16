package cn.zz.xposed.methodwatcher.data.repository.methods

import cn.zz.xposed.methodwatcher.data.DataResult
import cn.zz.xposed.methodwatcher.data.MonitorMethod
import cn.zz.xposed.methodwatcher.data.MonitorMethodGroup
import cn.zz.xposed.methodwatcher.data.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update

class MonitorMethodsSelectorRepository(private val monitoringMethods: List<MonitorMethod>) :
    Repository {

    private val groups = MonitorMethodGroup.allGroups

    private val methods: Map<MonitorMethodGroup, MutableStateFlow<List<MonitorMethodItemData>>> by lazy {
        MonitorMethod.allMethods.groupBy(
            keySelector = { it.group },
            valueTransform = { MonitorMethodItemData(it, it in monitoringMethods) })
            .mapValues {
                MutableStateFlow(it.value)
            }
    }

    /**
     * 加载监控方法分组数据
     */
    suspend fun loadGroups(): DataResult<List<MonitorMethodGroup>> {
        return DataResult.Success(groups)
    }

    /**
     * 监听[group]分组的方法数据
     */
    fun observeMethodsOfGroup(group: MonitorMethodGroup): Flow<List<MonitorMethodItemData>> {
        return methods[group] ?: emptyFlow()
    }

    /**
     * 重置数据
     */
    suspend fun reset() {
        for ((_, flow) in methods) {
            val list = flow.value.map { if (!it.isSelected) it.copy(isSelected = true) else it }
            flow.update { list }
        }
    }

    /**
     * 切换方法选中状态
     */
    suspend fun toggleMethodSelected(data: MonitorMethodItemData) {
        val flow = methods[data.method.group] ?: return
        val list = flow.value
        val index = list.indexOf(data)
        if (index < 0) {
            return
        }
        val updateList = list.toMutableList()
        updateList[index] = data.copy(isSelected = !data.isSelected)
        flow.update {
            updateList
        }
    }

    suspend fun select(): DataResult<List<MonitorMethod>> {
        val list = mutableListOf<MonitorMethod>()
        for ((_, flow) in methods) {
            flow.value.mapNotNullTo(list) {
                if (it.isSelected) it.method else null
            }
        }
        return DataResult.Success(list)
    }
}