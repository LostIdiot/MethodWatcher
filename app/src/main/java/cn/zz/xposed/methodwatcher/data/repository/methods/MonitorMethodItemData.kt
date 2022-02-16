package cn.zz.xposed.methodwatcher.data.repository.methods

import androidx.compose.runtime.Stable
import cn.zz.xposed.methodwatcher.data.MonitorMethod

@Stable
data class MonitorMethodItemData(
    val method: MonitorMethod,
    val isSelected: Boolean
)