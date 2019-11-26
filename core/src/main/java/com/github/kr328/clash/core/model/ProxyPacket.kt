package com.github.kr328.clash.core.model

import android.os.Parcel
import android.os.Parcelable
import com.github.kr328.clash.core.serialization.Parcels
import kotlinx.serialization.Serializable

@Serializable
data class ProxyPacket(val proxies: Map<String, Proxy>) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        Parcels.dump(serializer(), this, parcel)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProxyPacket> {
        override fun createFromParcel(parcel: Parcel): ProxyPacket {
            return Parcels.load(serializer(), parcel)
        }

        override fun newArray(size: Int): Array<ProxyPacket?> {
            return arrayOfNulls(size)
        }
    }

}