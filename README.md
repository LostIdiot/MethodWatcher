# MethodWatcher

App方法调用监听

基于[LSPosed](https://github.com/LSPosed/LSPosed)

可用于监控敏感方法的调用, 辅助定位排查隐私合规方面的问题, 特别是第三方SDK对敏感方法的调用

## 功能

- 记录查看方法调用(时间, 调用栈等信息)
- 支持将调用记录导出为xlsx
- 支持针对不同应用配置需要监控的方法

## 使用

### 添加新的监控方法

在[`WatchedMethod`](app/src/main/java/cn/zz/xposed/methodwatcher/data/MonitorMethod.kt)中按照规则添加新的需要监控的方法即可, 如果需要增加新的方法分组, 则在[`WatchedMethodMethodGroup`](app/src/main/java/cn/zz/xposed/methodwatcher/data/MonitorMethodGroup.kt)中按照规则添加新的分组
