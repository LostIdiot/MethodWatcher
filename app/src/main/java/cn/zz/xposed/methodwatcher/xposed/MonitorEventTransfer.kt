package cn.zz.xposed.methodwatcher.xposed

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import cn.zz.xposed.methodwatcher.BuildConfig
import cn.zz.xposed.methodwatcher.MonitorInterface
import cn.zz.xposed.methodwatcher.data.MethodCallDetail
import cn.zz.xposed.methodwatcher.data.MonitorConfig
import cn.zz.xposed.methodwatcher.data.MonitorEvent
import cn.zz.xposed.methodwatcher.data.MonitorTargetInfo
import cn.zz.xposed.methodwatcher.extra.binder.BinderContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MonitorEventTransfer(
    private val context: Context,
    processName: String,
    scope: CoroutineScope
) {

    private var service: MonitorInterface? = null
    private lateinit var connection: ServiceConnection

    private val self = MonitorTargetInfo(
        context.packageName,
        processName
    )

    private val binderContainer = BinderContainer(Binder())

    private val eventChannel =
        Channel<MonitorEvent>(Channel.Factory.BUFFERED, BufferOverflow.DROP_OLDEST)

    init {
        scope.launch {
            eventChannel.consumeAsFlow()
                .collect {
                    bindService()

                    when (it) {
                        is MonitorEvent.OnStart -> {
                            service?.onMonitorStart(self, binderContainer)
                        }
                        is MonitorEvent.OnCalledSystem -> {
                            service?.onCallMethod(
                                self,
                                it.detail
                            )
                        }
                    }
                }
        }
    }

    fun sendStart() {
        eventChannel.trySend(MonitorEvent.OnStart)
    }

    fun sendSystemCall(detail: MethodCallDetail) {
        eventChannel.trySend(
            MonitorEvent.OnCalledSystem(
                detail
            )
        )
    }

    suspend fun loadMonitorConfig(): MonitorConfig? {
        bindService()
        return service?.loadMonitorConfig(self)
    }

    fun destroy() {
        eventChannel.close()
        if (this::connection.isInitialized) {
            context.unbindService(connection)
        }
    }

    private suspend fun bindService() {
        if (service != null) {
            return
        }
        service = suspendCoroutine { continuation ->
            val intent = Intent()
                .setComponent(
                    ComponentName(
                        BuildConfig.APPLICATION_ID,
                        "cn.zz.xposed.methodwatcher.service.MonitorService"
                    )
                )
            connection = object : ServiceConnection {
                override fun onServiceConnected(
                    name: ComponentName?,
                    service: IBinder?
                ) {
                    continuation.resume(MonitorInterface.Stub.asInterface(service))
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    service = null
                }
            }
            if (!context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
                continuation.resume(null)
            }
        }
    }
}