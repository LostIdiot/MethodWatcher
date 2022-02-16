package cn.zz.xposed.methodwatcher.ui.target

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.zz.xposed.methodwatcher.data.MonitorMethod
import cn.zz.xposed.methodwatcher.data.repository.target.MonitorTargetRepository
import cn.zz.xposed.methodwatcher.db.MonitorConfigEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MonitorTargetViewModel(packageName: String) : ViewModel() {

    private val repository = MonitorTargetRepository(packageName)

    private var currentConfig: MonitorConfigEntity = MonitorConfigEntity(packageName = packageName)

    val enabled: Flow<Boolean>
        get() = _enabled
    private val _enabled = MutableStateFlow(true)

    val monitorMethods: Flow<List<MonitorMethod>>
        get() = _monitorMethods
    private val _monitorMethods = MutableStateFlow<List<MonitorMethod>>(emptyList())

    val recordsCountData = repository.observeRecordsCount()
        .distinctUntilChanged()

    init {
        val configFlow = repository.observeConfig()
            .map {
                it ?: MonitorConfigEntity(packageName = packageName)
            }

        viewModelScope.launch {
            configFlow.collect {
                currentConfig = it
            }
        }

        viewModelScope.launch {
            configFlow.collect { data ->
                _enabled.update {
                    data.enabled
                }
            }
        }

        viewModelScope.launch {
            configFlow.collect { data ->
                _monitorMethods.update {
                    data.watchedMethods
                }
            }
        }
    }

    fun enabled(enabled: Boolean) {
        updateConfig(
            currentConfig.copy(
                enabled = enabled,
                modifiedTime = System.currentTimeMillis()
            )
        )
    }

    fun monitorMethods(list: List<MonitorMethod>) {
        updateConfig(
            currentConfig.copy(
                watchedMethods = list,
                modifiedTime = System.currentTimeMillis()
            )
        )
    }

    fun deleteRecords() {
        viewModelScope.launch {
            repository.deleteRecords()
        }
    }

    private fun updateConfig(config: MonitorConfigEntity) {
        viewModelScope.launch {
            repository.updateConfig(config)
        }
    }
}