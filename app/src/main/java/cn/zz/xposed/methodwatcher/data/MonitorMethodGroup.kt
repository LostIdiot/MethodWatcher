package cn.zz.xposed.methodwatcher.data

/**
 * the group of methods you want to monitor
 *
 * 要监听的方法分组
 *
 * @param groupName group name, unique.
 *                  分组名称, 需唯一
 */
sealed class MonitorMethodGroup(val groupName: String) {

    object PackageManager : MonitorMethodGroup("PackageManager")

    object TelephonyManager : MonitorMethodGroup("TelephonyManager")

    object AccessibilityService : MonitorMethodGroup("AccessibilityService")

    object Default : MonitorMethodGroup("Default")

    companion object {

        /**
         * 全部分组
         */
        val allGroups by lazy {
            listOf(
                PackageManager,
                TelephonyManager,
                AccessibilityService,
                Default
            )
        }
    }
}
