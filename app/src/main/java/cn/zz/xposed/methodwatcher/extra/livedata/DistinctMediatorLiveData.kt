package cn.zz.xposed.methodwatcher.extra.livedata

import androidx.lifecycle.MediatorLiveData

class DistinctMediatorLiveData<T> : MediatorLiveData<T>() {

    override fun setValue(value: T) {
        if (getValue() != value) {
            super.setValue(value)
        }
    }
}