package cn.zz.xposed.methodwatcher.data

import android.os.Build
import android.telephony.TelephonyManager
import android.view.accessibility.AccessibilityManager
import kotlin.collections.HashMap

/**
 * The method you want monitor
 *
 * 想要监控的方法
 */
sealed class MonitorMethod(
    /**
     * method id, unique.
     *
     * 方法id, 需唯一
     */
    val id: Int,

    /**
     * class name of the method
     *
     * 方法所在类的全类名
     */
    val className: String,

    /**
     * method name
     *
     * 方法名
     */
    val methodName: String,

    /**
     * method parameter types
     *
     * 方法参数类型
     */
    val paramTypes: Array<out Class<*>> = emptyArray(),

    /**
     * group
     *
     * 分组
     * @see MonitorMethodGroup
     */
    val group: MonitorMethodGroup = MonitorMethodGroup.Default,

    /**
     * the api level range for method to hook
     * [IntRange.EMPTY] means no restrict, it will be hooked for all api levels
     *
     * 方法监控生效的api level, 用于处理不同版本的兼容问题
     * [IntRange.EMPTY]表示没有限制, 所有版本均生效
     */
    val apiLevelRange: IntRange = IntRange.EMPTY
) {

    //region PackageManager
    /**
     * [android.content.pm.PackageManager.getInstalledPackages]
     */
    object GetInstalledPackages : MonitorMethod(
        1,
        "android.app.ApplicationPackageManager",
        "getInstalledPackages",
        arrayOf(Int::class.java),
        MonitorMethodGroup.PackageManager
    )
    //endregion

    //region TelephonyManager
    /**
     * [TelephonyManager.getImei]
     */
    object GetIMEI : MonitorMethod(
        2,
        TelephonyManager::class.java.canonicalName.orEmpty(),
        "getImei",
        group = MonitorMethodGroup.TelephonyManager,
        apiLevelRange = Build.VERSION_CODES.O..Int.MAX_VALUE
    )

    /**
     * [TelephonyManager.getDeviceId]
     */
    object GetDeviceId : MonitorMethod(
        3,
        TelephonyManager::class.java.canonicalName.orEmpty(),
        "getDeviceId",
        group = MonitorMethodGroup.TelephonyManager,
        apiLevelRange = 1 until Build.VERSION_CODES.O
    )

    /**
     * [TelephonyManager.getLine1Number]
     */
    object GetLine1Number : MonitorMethod(
        4,
        TelephonyManager::class.java.canonicalName.orEmpty(),
        "getLine1Number",
        group = MonitorMethodGroup.TelephonyManager
    )
    //endregion

    //region AccessibilityManager
    /**
     * [AccessibilityManager.getEnabledAccessibilityServiceList]
     */
    object GetEnabledAccessibilityServiceList : MonitorMethod(
        5,
        AccessibilityManager::class.java.canonicalName.orEmpty(),
        "getEnabledAccessibilityServiceList",
        arrayOf(Int::class.java),
        MonitorMethodGroup.AccessibilityService
    )

    /**
     * [AccessibilityManager.getInstalledAccessibilityServiceList]
     */
    object GetInstalledAccessibilityServiceList : MonitorMethod(
        6,
        AccessibilityManager::class.java.canonicalName.orEmpty(),
        "getInstalledAccessibilityServiceList",
        group = MonitorMethodGroup.AccessibilityService
    )

    /**
     * [AccessibilityManager.getAccessibilityServiceList]
     */
    object GetAccessibilityServiceList : MonitorMethod(
        7,
        AccessibilityManager::class.java.canonicalName.orEmpty(),
        "getAccessibilityServiceList",
        group = MonitorMethodGroup.AccessibilityService
    )
    //endregion

    //region Unknown
    /**
     * unknown method
     *
     * @see MonitorMethod.findById
     * @see MonitorConfig
     */
    object Unknown : MonitorMethod(-1, "", "")
    //endregion

    //region companion
    companion object {

        /**
         * get method from
         * return [Unknown] for unknown id
         *
         * 根据id获取[MonitorMethod]
         * 如果是未知id, 则返回[Unknown]
         */
        fun findById(id: Int): MonitorMethod {
            return when (id) {
                GetInstalledPackages.id -> GetInstalledPackages
                GetIMEI.id -> GetIMEI
                GetDeviceId.id -> GetDeviceId
                GetLine1Number.id -> GetLine1Number
                GetEnabledAccessibilityServiceList.id -> GetEnabledAccessibilityServiceList
                GetInstalledAccessibilityServiceList.id -> GetInstalledAccessibilityServiceList
                GetAccessibilityServiceList.id -> GetAccessibilityServiceList
                else -> Unknown
            }
        }

        /**
         * all methods list
         *
         * 所有方法的列表
         */
        val allMethods by lazy {
            val list = listOf(
                GetInstalledPackages,
                GetIMEI,
                GetDeviceId,
                GetLine1Number,
                GetEnabledAccessibilityServiceList,
                GetInstalledAccessibilityServiceList,
                GetAccessibilityServiceList
            )

            // 检查是否有重复id
            val map = HashMap<Int, MonitorMethod>(list.size)
            for (method in list) {
                if (map.containsKey(method.id)) {
                    val exist = map[method.id]
                    throw RuntimeException("found duplicated method id: $method and $exist")
                }
            }

            list
        }
    }
    //endregion
}
