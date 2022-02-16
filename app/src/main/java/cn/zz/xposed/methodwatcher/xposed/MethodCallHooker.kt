package cn.zz.xposed.methodwatcher.xposed

import android.app.AndroidAppHelper
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import cn.zz.xposed.methodwatcher.data.MonitorConfig
import cn.zz.xposed.methodwatcher.data.MonitorMethod
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.getMethodByClassOrObject
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import de.robv.android.xposed.XC_MethodHook
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

class MethodCallHooker(private val context: Context) : CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + SupervisorJob()

    private val transfer =
        MonitorEventTransfer(context, AndroidAppHelper.currentProcessName(), this)

    private val hookers = mutableMapOf<MonitorMethod, XC_MethodHook.Unhook>()

    private val stackTraceHelper = CallTraceHelper()

    private val mutex = Mutex()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_UPDATE_MONITOR_CONFIG -> updateMonitorConfig()
            }
        }
    }

    fun startHook() {
        registerReceiver()
        transfer.sendStart()
        updateMonitorConfig()
    }

    fun destroy() {
        cancel()
        transfer.destroy()
        context.unregisterReceiver(receiver)
    }

    private fun registerReceiver() {
        val filter = IntentFilter(ACTION_UPDATE_MONITOR_CONFIG)
        context.registerReceiver(receiver, filter)
    }

    private fun hook(config: MonitorConfig) {
        val methods = config.methods
        // remove old hookers if not in new config
        val iterator = hookers.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (!methods.contains(entry.key)) {
                entry.value.unhook()
                iterator.remove()
            }
        }
        for (method in config.methods) {
            hookMethod(method)
        }
    }

    /**
     * hook some method
     */
    private fun hookMethod(method: MonitorMethod) {
        if (method == MonitorMethod.Unknown || hookers.containsKey(method)) {
            return
        }

        if (!method.apiLevelRange.isEmpty() && Build.VERSION.SDK_INT !in method.apiLevelRange) {
            return
        }

        try {
            val m = Class.forName(method.className)
                .getMethodByClassOrObject(method.methodName, argTypes = method.paramTypes)
            val unhook = m.hookAfter {
                val e = Exception()
                val detail = stackTraceHelper.parseCallTrace(method, e.stackTrace)
                if (detail != null) {
                    transfer.sendSystemCall(detail)
                }
            }
            hookers[method] = unhook
            Log.d("hook ${method.className}.${method.methodName} success")
        } catch (e: Exception) {
            Log.d("hook ${method.className}.${method.methodName} failed", e)
        }
    }

    private fun updateMonitorConfig() {
        launch {
            val config = transfer.loadMonitorConfig() ?: return@launch
            mutex.withLock {
                hook(config)
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_MONITOR_CONFIG = "update_monitor_config"
    }
}