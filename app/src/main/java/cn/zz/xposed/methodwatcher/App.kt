package cn.zz.xposed.methodwatcher

import android.app.Application
import android.content.Context
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit

class App : Application() {

    override fun attachBaseContext(base: Context?) {
        appInternal = this
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        EzXHelperInit.setLogTag("SystemCallMonitor")
    }

}

val app: Application
    get() = appInternal
private lateinit var appInternal: Application