package com.github.kr328.clash.core

import android.os.Parcel
import android.os.Parcelable

data class ClashProcessStatus(val status: Int) : Parcelable {
    companion object {
        const val STATUS_STOPPED = 0
        const val STATUS_STARTED = 1

        @JvmField
        val CREATOR = object: Parcelable.Creator<ClashProcessStatus> {
            override fun createFromParcel(parcel: Parcel): ClashProcessStatus {
                return ClashProcessStatus(parcel)
            }

            override fun newArray(size: Int): Array<ClashProcessStatus?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcel: Parcel) : this(parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(status)
    }

    override fun describeContents(): Int {
        return 0
    }
}