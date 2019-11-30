package com.github.kr328.clash.core.model

import kotlinx.serialization.Serializable

class LoadProfilePacket {
    @Serializable
    data class Request(val path: String, val selected: Map<String, String>)
    @Serializable
    data class Response(val error: String, val invalidSelected: List<String>)
}