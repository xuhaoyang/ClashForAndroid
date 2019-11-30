package com.github.kr328.clash.core.event

import android.os.Parcel
import android.os.Parcelable
import com.github.kr328.clash.core.serialization.Parcels
import kotlinx.serialization.Serializable

@Serializable
data class BandwidthEvent(val total: Long): Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        Parcels.dump(serializer(), this, parcel)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BandwidthEvent> {
        override fun createFromParcel(parcel: Parcel): BandwidthEvent {
            return Parcels.load(serializer(), parcel)
        }

        override fun newArray(size: Int): Array<BandwidthEvent?> {
            return arrayOfNulls(size)
        }
    }

}