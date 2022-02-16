package cn.zz.xposed.methodwatcher.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cn.zz.xposed.methodwatcher.R
import cn.zz.xposed.methodwatcher.db.MonitorDatabase
import cn.zz.xposed.methodwatcher.monitor.monitorManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class MonitorService : Service(), CoroutineScope by MainScope() {

    private val binder = MonitorBinder()

    private val notificationId = 14537
    private val notificationChannelId = "MONITOR_STATE"
    private val notificationManager by lazy { NotificationManagerCompat.from(this) }
    private val notificationBuilder: NotificationCompat.Builder by lazy {
        val channel = NotificationChannelCompat.Builder(
            notificationChannelId,
            NotificationManagerCompat.IMPORTANCE_LOW
        )
            .setName("监听状态通知")
            .setDescription("发送方法调用监听状态相关的通知")
            .build()
        notificationManager.createNotificationChannel(channel)

        NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("监听状态")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentText("正在监控的应用: 0\n共记录到0次方法调用")
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(notificationId, notificationBuilder.build())

        launch {
            monitorManager.activeCountState
                .combine(
                    MonitorDatabase.database.recordDao().observeRecordCount()
                        .flowOn(Dispatchers.IO)
                ) { v1: Int, v2: Int ->
                    v1 to v2
                }
                .collect {
                    notificationBuilder.setContentText("正在监控的应用: ${it.first}\n共记录到${it.second}次方法调用")
                    notificationManager.notify(notificationId, notificationBuilder.build())
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }

}