package cn.zz.xposed.methodwatcher.xposed

import android.app.AndroidAppHelper
import cn.zz.xposed.methodwatcher.BuildConfig
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedEntry : IXposedHookZygoteInit, IXposedHookLoadPackage {

    private lateinit var hooker: MethodCallHooker

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        EzXHelperInit.initHandleLoadPackage(lpparam)

        if (BuildConfig.APPLICATION_ID == lpparam.packageName) {
            // 自己不用监听自己
            return
        }

        if (this::hooker.isInitialized) {
            // 已经hook过了, 直接返回
            return
        }

        EzXHelperInit.setLogTag("SystemCallMonitor")

        hooker = MethodCallHooker(AndroidAppHelper.currentApplication())
        hooker.startHook()
    }
}