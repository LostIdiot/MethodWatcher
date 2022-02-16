package cn.zz.xposed.methodwatcher.data.repository.apps

import android.content.pm.PackageInfo
import androidx.compose.runtime.Stable
import cn.zz.xposed.methodwatcher.db.MonitorConfigEntity

@Stable
data class AppListItemData(
    val packageInfo: PackageInfo,
    val config: MonitorConfigEntity?
)