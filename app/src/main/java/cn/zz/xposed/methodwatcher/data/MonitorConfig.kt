package cn.zz.xposed.methodwatcher.data

import android.os.Parcel
import android.os.Parcelable

data class MonitorConfig(
    val methods: Set<MonitorMethod>
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.createIntArray()?.mapTo(mutableSetOf()) {
            MonitorMethod.findById(it)
        }.orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeIntArray(methods.map { it.id }.toIntArray())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MonitorConfig> {
        override fun createFromParcel(parcel: Parcel): MonitorConfig {
            return MonitorConfig(parcel)
        }

        override fun newArray(size: Int): Array<MonitorConfig?> {
            return arrayOfNulls(size)
        }
    }
}