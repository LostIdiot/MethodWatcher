package cn.zz.xposed.methodwatcher.ui.record

import cn.zz.xposed.methodwatcher.data.repository.record.TargetAppItemData
import cn.zz.xposed.methodwatcher.db.MonitorRecordEntity

data class MethodCallRecordData(
    val entity: MonitorRecordEntity,
    val app: TargetAppItemData?
)