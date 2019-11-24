package com.github.kr328.clash.core.model

import android.os.Parcel
import android.os.Parcelable
import com.github.kr328.clash.core.serialization.Parcels
import kotlinx.serialization.Serializable

@Serializable
enum class ProcessEvent : Parcelable, Event {
    STARTED, STOPPED;

    companion object CREATOR : Parcelable.Creator<ProcessEvent> {
        override fun createFromParcel(parcel: Parcel): ProcessEvent {
            return Parcels.load(serializer(), parcel)
        }

        override fun newArray(size: Int): Array<ProcessEvent?> {
            return arrayOfNulls(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        Parcels.dump(serializer(), this, parcel)
    }

    override fun describeContents(): Int {
        return 0
    }
}