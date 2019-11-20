package com.github.kr328.clash.core

import android.os.Parcel
import android.os.Parcelable

data class ClashStatus(val status: Int) : Parcelable {
    companion object {
        const val STOPPED = 0
        const val IDLE = 1
        const val LOADING_PROFILE = 2

        @JvmField
        val CREATOR = object: Parcelable.Creator<ClashStatus> {
            override fun createFromParcel(parcel: Parcel): ClashStatus {
                return ClashStatus(parcel)
            }

            override fun newArray(size: Int): Array<ClashStatus?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcel: Parcel) : this(parcel.readInt())

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(status)
    }

    override fun describeContents(): Int {
        return 0
    }


}