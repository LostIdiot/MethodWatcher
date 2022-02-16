package cn.zz.xposed.methodwatcher.data.repository.record

import cn.zz.xposed.methodwatcher.data.MonitorMethod

data class MonitorMethodItemData(
    val method: MonitorMethod,
    val selected: Boolean = true
)