package com.github.kr328.clash.core.model

import android.os.Parcel
import android.os.Parcelable
import com.github.kr328.clash.core.serialization.Parcels
import kotlinx.serialization.Serializable

@Serializable
data class Proxy(val type: String,
                 val all: List<String> = emptyList(),
                 val now: String = "",
                 val history: List<String> = emptyList())