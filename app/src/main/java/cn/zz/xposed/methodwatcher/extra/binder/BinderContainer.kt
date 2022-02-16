package cn.zz.xposed.methodwatcher.extra.binder

import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable

class BinderContainer(val binder: IBinder) : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readStrongBinder())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeStrongBinder(binder)
    }

    companion object CREATOR : Parcelable.Creator<BinderContainer> {
        override fun createFromParcel(parcel: Parcel): BinderContainer {
            return BinderContainer(parcel)
        }

        override fun newArray(size: Int): Array<BinderContainer?> {
            return arrayOfNulls(size)
        }
    }

}