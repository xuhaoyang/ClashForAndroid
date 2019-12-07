package com.github.kr328.clash.core.model

import kotlinx.serialization.Serializable

@Serializable
data class RawProxyPacket(val mode: String, val proxies: Map<String, RawProxy>) {
    @Serializable
    data class RawProxy(val type: String,
                        val all: List<String> = emptyList(),
                        val order: Int = 0,
                        val now: String = "",
                        val history: List<History> = emptyList()) {
        @Serializable
        data class History(val delay: Long)
    }
}