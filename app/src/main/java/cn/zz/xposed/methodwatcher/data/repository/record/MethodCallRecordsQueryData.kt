package cn.zz.xposed.methodwatcher.data.repository.record

import cn.zz.xposed.methodwatcher.data.MonitorMethod

/**
 * 调用记录查询参数数据类
 */
data class MethodCallRecordsQueryData(
    /**
     * 要查询的包名
     */
    val targetPackageNames: List<String>,

    /**
     * 要查询的方法
     */
    val methods: List<MonitorMethod>
)