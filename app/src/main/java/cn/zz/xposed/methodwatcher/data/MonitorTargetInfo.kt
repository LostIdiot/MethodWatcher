package cn.zz.xposed.methodwatcher.data

import android.os.Parcel
import android.os.Parcelable

/**
 * 被监听App的信息
 */
data class MonitorTargetInfo(
    val packageName: String,
    val processName: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeString(processName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MonitorTargetInfo> {
        override fun createFromParcel(parcel: Parcel): MonitorTargetInfo {
            return MonitorTargetInfo(parcel)
        }

        override fun newArray(size: Int): Array<MonitorTargetInfo?> {
            return arrayOfNulls(size)
        }
    }

}