package com.github.kr328.clash.model

import com.github.kr328.clash.core.model.ProxyPacket

data class ListProxyGroup(
    val name: String,
    val type: ProxyPacket.Type,
    val proxies: List<ListProxy>,
    var now: ListProxy?
) {
    data class ListProxy(val name: String, val type: String, val delay: Long)
}