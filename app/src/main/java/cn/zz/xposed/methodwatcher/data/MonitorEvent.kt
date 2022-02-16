package cn.zz.xposed.methodwatcher.data

/**
 * 监听事件
 */
sealed class MonitorEvent {

    /**
     * 开始监听事件
     */
    object OnStart : MonitorEvent()

    /**
     * 调用了系统方法
     */
    class OnCalledSystem(
        val detail: MethodCallDetail,
    ) : MonitorEvent()
}

