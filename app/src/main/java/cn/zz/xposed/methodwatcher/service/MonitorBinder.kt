package cn.zz.xposed.methodwatcher.service

import cn.zz.xposed.methodwatcher.MonitorInterface
import cn.zz.xposed.methodwatcher.data.MethodCallDetail
import cn.zz.xposed.methodwatcher.data.MonitorConfig
import cn.zz.xposed.methodwatcher.data.MonitorMethod
import cn.zz.xposed.methodwatcher.data.MonitorTargetInfo
import cn.zz.xposed.methodwatcher.extra.binder.BinderContainer
import cn.zz.xposed.methodwatcher.monitor.monitorManager

class MonitorBinder : MonitorInterface.Stub() {

    /**
     * default monitor config, contains all methods
     *
     * @see MonitorMethod.allMethods
     */
    private val defaultMonitorConfig = MonitorConfig(MonitorMethod.allMethods.toSet())

    override fun loadMonitorConfig(target: MonitorTargetInfo?): MonitorConfig? {
        if (target == null) {
            return null
        }
        return defaultMonitorConfig
    }

    override fun onMonitorStart(target: MonitorTargetInfo?, binderContainer: BinderContainer?) {
        if (target == null || binderContainer == null) {
            return
        }

        monitorManager.onTargetConnected(target, binderContainer)
    }

    override fun onCallMethod(target: MonitorTargetInfo?, detail: MethodCallDetail?) {
        if (target == null) {
            return
        }

        if (detail == null) {
            return
        }

        if (detail.callTrace.isEmpty()) {
            return
        }

        if (detail.method == MonitorMethod.Unknown) {
            return
        }

        monitorManager.onMethodCalled(target, detail)
    }
}