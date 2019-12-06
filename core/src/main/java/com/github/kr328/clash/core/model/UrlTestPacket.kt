package com.github.kr328.clash.core.model

import kotlinx.serialization.Serializable

class UrlTestPacket {
    @Serializable
    data class Request(val proxies: List<String>, val timeout: Int, val url: String)
    @Serializable
    data class Response(val name: String, val delay: Long)
}

