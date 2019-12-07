package com.github.kr328.clash.core.model

import android.os.Parcel
import android.os.Parcelable
import com.github.kr328.clash.core.serialization.Parcels
import kotlinx.serialization.Serializable

@Serializable
data class ProxyPacket(val mode: String, val proxies: Map<Int, Proxy>): Parcelable {
    @Serializable
    data class Proxy(val name: String, val type: Type, val order: Int, val now: Int, val all: Set<Int>, val delay: Long)

    enum class Type {
        SELECT,
        URL_TEST,
        FALLBACK,
        DIRECT,
        REJECT,
        SHADOWSOCKS,
        SNELL,
        SOCKS5,
        HTTP,
        VMESS,
        LOAD_BALANCE,
        UNKNOWN;

        override fun toString(): String {
            return when ( this ) {
                SELECT -> "Select"
                URL_TEST -> "UrlTest"
                FALLBACK -> "Fallback"
                DIRECT -> "Direct"
                REJECT -> "Reject"
                SHADOWSOCKS -> "Shadowsocks"
                SNELL -> "Snell"
                SOCKS5 -> "Socks5"
                HTTP -> "HTTP"
                VMESS -> "Vmess"
                LOAD_BALANCE -> "LoadBalance"
                UNKNOWN -> "Unknown"
            }
        }
    }

    companion object {
        private const val TYPE_SELECT = "Selector"
        private const val TYPE_URL_TEST = "URLTest"
        private const val TYPE_FALLBACK = "Fallback"
        private const val TYPE_DIRECT = "Direct"
        private const val TYPE_REJECT = "Reject"
        private const val TYPE_SHADOWSOCKS = "Shadowsocks"
        private const val TYPE_SNELL = "Snell"
        private const val TYPE_SOCKS5 = "Socks5"
        private const val TYPE_HTTP = "Http"
        private const val TYPE_VMESS = "Vmess"
        private const val TYPE_LOAD_BALANCE = "LoadBalance"
        private const val TYPE_UNKNOWN = "Unknown"

        fun fromRawProxy(rawProxy: RawProxyPacket): ProxyPacket {
            val hashed = rawProxy.proxies
                .map { it.key to (it.key.hashCode() to it.value) }.toMap()

            val proxies = hashed.map { entry ->
                val type = when ( entry.value.second.type ) {
                    TYPE_SELECT -> Type.SELECT
                    TYPE_URL_TEST -> Type.URL_TEST
                    TYPE_FALLBACK -> Type.FALLBACK
                    TYPE_DIRECT -> Type.DIRECT
                    TYPE_REJECT -> Type.REJECT
                    TYPE_SHADOWSOCKS -> Type.SHADOWSOCKS
                    TYPE_SNELL -> Type.SNELL
                    TYPE_SOCKS5 -> Type.SOCKS5
                    TYPE_HTTP -> Type.HTTP
                    TYPE_VMESS -> Type.VMESS
                    TYPE_LOAD_BALANCE -> Type.LOAD_BALANCE
                    TYPE_UNKNOWN -> Type.UNKNOWN
                    else -> Type.UNKNOWN
                }

                val now = hashed[entry.value.second.now]?.first ?: 0
                val all = entry.value.second.all.mapNotNull { hashed[it]?.first }.toSet()
                val delay = entry.value.second.history.firstOrNull()?.delay ?: 0

                entry.value.first to Proxy(entry.key, type,entry.value.second.order, now, all, delay)
            }

            return ProxyPacket(rawProxy.mode, proxies.toMap())
        }

        @JvmField
        val CREATOR = object: Parcelable.Creator<ProxyPacket> {
            override fun createFromParcel(parcel: Parcel): ProxyPacket {
                return Parcels.load(serializer(), parcel)
            }

            override fun newArray(size: Int): Array<ProxyPacket?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        Parcels.dump(serializer(), this, parcel)
    }

    override fun describeContents(): Int {
        return 0
    }
}