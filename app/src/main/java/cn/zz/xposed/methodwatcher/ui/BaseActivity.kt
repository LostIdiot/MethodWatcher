package cn.zz.xposed.methodwatcher.ui

import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat

open class BaseActivity @JvmOverloads constructor(@LayoutRes id: Int = ResourcesCompat.ID_NULL) :
    AppCompatActivity(id)