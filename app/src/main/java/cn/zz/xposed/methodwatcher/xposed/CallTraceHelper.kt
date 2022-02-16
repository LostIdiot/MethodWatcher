package cn.zz.xposed.methodwatcher.xposed

import cn.zz.xposed.methodwatcher.data.MethodCallDetail
import cn.zz.xposed.methodwatcher.data.MonitorMethod

/**
 * 处理调用追踪的辅助类
 */
class CallTraceHelper {

    /**
     * 需要忽略的类名前缀
     * 即stack trace中出现了匹配的条目, 则直接忽略并返回""
     */
    private val ignoredClassNamePrefix = listOf<String>()

    /**
     * 需要排除的类名前缀
     * 即stack trace中出现了匹配的条目, 则移除该条element
     */
    private val excludeClassNamePrefix = listOf(
        // 自己
        "cn.zz.xposed.methodwatcher",
        // lsposed
        "de.robv.android.xposed",
        "LspHooker_",
        // EzXHelper
        "com.github.kyuubiran.ezxhelper"
    )

    /**
     * 解析调用堆栈并进行初步处理
     * 排除不必要的退栈信息, 减少生成的数据大小
     */
    fun parseCallTrace(
        method: MonitorMethod,
        stackTrace: Array<StackTraceElement>
    ): MethodCallDetail? {
        if (stackTrace.isEmpty()) {
            return null
        }
        val trace = stackTrace.toMutableList()
        // 过滤堆栈
        val iterator = trace.iterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            for (prefix in ignoredClassNamePrefix) {
                if (element.className.startsWith(prefix)) {
                    return null
                }
            }
            for (prefix in excludeClassNamePrefix) {
                if (element.className.startsWith(prefix)) {
                    iterator.remove()
                }
            }
        }

        if (trace.isEmpty()) {
            // 全部过滤完了
            return null
        }

        // 直接调用者
        val directCaller = "${trace[0].className}.${trace[0].methodName}"

        // 调用堆栈
        val traceString = trace.joinToString("\nat ")
        return MethodCallDetail(method, traceString, directCaller)
    }
}