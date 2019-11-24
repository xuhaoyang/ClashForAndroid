package com.github.kr328.clash.core.transact

import kotlinx.serialization.Serializable

@Serializable
data class Proxy(val type: String) {
    @Serializable
    data class Packet(val proxies: Map<String, Proxy>)
}