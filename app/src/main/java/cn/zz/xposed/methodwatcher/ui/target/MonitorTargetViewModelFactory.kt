package cn.zz.xposed.methodwatcher.ui.target

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MonitorTargetViewModelFactory(private val packageName: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass == MonitorTargetViewModel::class.java) {
            return MonitorTargetViewModel(packageName) as T
        } else {
            throw RuntimeException("Cannot create an instance of $modelClass")
        }
    }
}