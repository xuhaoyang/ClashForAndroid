package com.github.kr328.clash.core.model

import android.os.Parcel
import android.os.Parcelable
import com.github.kr328.clash.core.serialization.Parcels
import kotlinx.serialization.Serializable

@Serializable
data class Proxy(val type: String,
                 val all: List<String> = emptyList(),
                 val now: String = "",
                 val history: List<History> = emptyList()) {
    @Serializable
    data class History(val delay: Long)

    companion object {
        const val TYPE_SELECT = "Selector"
        const val TYPE_URL_TEST = "URLTest"
        const val TYPE_DIRECT = "Direct"
        const val TYPE_FALLBACK = "Fallback"
        const val TYPE_REJECT = "Reject"
        const val TYPE_SHADOWSOCKS = "Shadowsocks"
        const val TYPE_SNELL = "Snell"
        const val TYPE_SOCKS5 = "Socks5"
        const val TYPE_HTTP = "Http"
        const val TYPE_VMESS = "Vmess"
        const val TYPE_LOAD_BALANCE = "LoadBalance"
        const val TYPE_UNKNOWN = "Unknown"
    }
}