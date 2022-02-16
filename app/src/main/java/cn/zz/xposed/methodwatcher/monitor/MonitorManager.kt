package cn.zz.xposed.methodwatcher.monitor

import android.os.IBinder
import cn.zz.xposed.methodwatcher.data.MethodCallDetail
import cn.zz.xposed.methodwatcher.data.MonitorTargetInfo
import cn.zz.xposed.methodwatcher.db.MonitorDatabase
import cn.zz.xposed.methodwatcher.db.MonitorRecordEntity
import cn.zz.xposed.methodwatcher.extra.binder.BinderContainer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

class MonitorManager : CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Default

    /**
     * 正在监听的应用计数
     */
    val activeCountState: StateFlow<Int>
        get() = _activeCountState
    private val _activeCountState = MutableStateFlow(0)

    /**
     * 正在监听的应用
     */
    private val activeTargets: MutableMap<MonitorTargetInfo, IBinder> = mutableMapOf()

    /**
     * 目标应用已连接
     */
    fun onTargetConnected(target: MonitorTargetInfo, binderContainer: BinderContainer) {
        synchronized(activeTargets) {
            if (activeTargets.containsKey(target)) {
                // 已连接
                return@synchronized
            }

            val binder = binderContainer.binder
            binder.linkToDeath({
                onTargetDisconnected(target)
            }, 0)

            activeTargets[target] = binder

            notifyActiveTargetsChanged()
        }
    }

    /**
     * 目标应用已断开连接
     */
    private fun onTargetDisconnected(target: MonitorTargetInfo) {
        synchronized(activeTargets) {
            if (activeTargets.remove(target) != null) {
                notifyActiveTargetsChanged()
            }
        }
    }

    private fun notifyActiveTargetsChanged() {
        _activeCountState.value = activeTargets.size
    }

    /**
     * 某个方法被调用了
     */
    fun onMethodCalled(target: MonitorTargetInfo, detail: MethodCallDetail) {
        val entity = MonitorRecordEntity(
            targetPackageName = target.packageName,
            targetProcessName = target.processName,
            method = detail.method,
            callTrace = detail.callTrace,
            directCaller = detail.directCaller,
            calledTime = detail.timestamp
        )
        launch {
            MonitorDatabase.database.recordDao().insertRecord(entity)
        }
    }

    fun destroy() {
        cancel()
    }
}

val monitorManager = MonitorManager()