package com.github.kr328.clash.core.model

import kotlinx.serialization.Serializable

class SetProxyPacket {
    @Serializable
    data class Request(val key: String, val value: String)
    @Serializable
    data class Response(val error: String)
}