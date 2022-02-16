package cn.zz.xposed.methodwatcher.data.repository.record

data class TargetAppItemData(
    val label: String,
    val packageName: String,
    val selected: Boolean = true
)