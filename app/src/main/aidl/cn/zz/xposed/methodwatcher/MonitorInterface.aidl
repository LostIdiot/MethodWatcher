package cn.zz.xposed.methodwatcher;

import cn.zz.xposed.methodwatcher.data.MonitorConfig;
import cn.zz.xposed.methodwatcher.data.MethodCallDetail;
import cn.zz.xposed.methodwatcher.data.MonitorTargetInfo;
import cn.zz.xposed.methodwatcher.extra.binder.BinderContainer;

interface MonitorInterface {

    MonitorConfig loadMonitorConfig(in MonitorTargetInfo target);

    void onMonitorStart(in MonitorTargetInfo target, in BinderContainer binderContainer);

    void onCallMethod(in MonitorTargetInfo target, in MethodCallDetail detail);
}