package com.github.kr328.clash.core.event

import android.os.Parcel
import android.os.Parcelable
import com.github.kr328.clash.core.serialization.Parcels
import kotlinx.serialization.Serializable

@Serializable
data class ErrorEvent(val type: Type, val message: String) : Event, Parcelable {
    enum class Type {
        START_FAILURE
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        Parcels.dump(serializer(), this, parcel)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ErrorEvent> {
        override fun createFromParcel(parcel: Parcel): ErrorEvent {
            return Parcels.load(serializer(), parcel)
        }

        override fun newArray(size: Int): Array<ErrorEvent?> {
            return arrayOfNulls(size)
        }
    }
}