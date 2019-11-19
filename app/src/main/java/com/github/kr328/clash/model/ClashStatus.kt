package com.github.kr328.clash.model

import android.os.Parcel
import android.os.Parcelable
import com.github.kr328.clash.core.ClashProcess

data class ClashStatus(val status: ClashProcess.Status) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        when ( status ) {
            ClashProcess.Status.STOPPED -> parcel.writeInt(0)
            ClashProcess.Status.STARTED -> parcel.writeInt(1)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ClashStatus> {
        override fun createFromParcel(parcel: Parcel): ClashStatus {
            return ClashStatus(readFromParcel(parcel))
        }

        override fun newArray(size: Int): Array<ClashStatus?> {
            return arrayOfNulls(size)
        }

        private fun readFromParcel(parcel: Parcel): ClashProcess.Status {
            return when ( parcel.readInt() ) {
                0 -> ClashProcess.Status.STOPPED
                1 -> ClashProcess.Status.STARTED
                else -> throw IllegalArgumentException()
            }
        }
    }


}