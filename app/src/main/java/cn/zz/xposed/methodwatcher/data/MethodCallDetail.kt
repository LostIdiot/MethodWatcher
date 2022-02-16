package cn.zz.xposed.methodwatcher.data

import android.os.Parcel
import android.os.Parcelable

/**
 * 方法调用详情
 */
data class MethodCallDetail(
    /**
     * 监听的方法
     */
    val method: MonitorMethod,
    /**
     * 方法调用堆栈
     */
    val callTrace: String,

    /**
     * 直接调用者
     */
    val directCaller: String,

    /**
     * 时间戳(毫秒)
     */
    val timestamp: Long = System.currentTimeMillis()
): Parcelable {
    constructor(parcel: Parcel) : this(
        MonitorMethod.findById(parcel.readInt()),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(method.id)
        parcel.writeString(callTrace)
        parcel.writeString(directCaller)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MethodCallDetail> {
        override fun createFromParcel(parcel: Parcel): MethodCallDetail {
            return MethodCallDetail(parcel)
        }

        override fun newArray(size: Int): Array<MethodCallDetail?> {
            return arrayOfNulls(size)
        }
    }

}