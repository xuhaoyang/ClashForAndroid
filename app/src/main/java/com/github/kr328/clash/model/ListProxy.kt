package com.github.kr328.clash.model

import com.github.kr328.clash.core.model.ProxyPacket

interface ListProxy {
    data class ListProxyHeader(val name: String, val type: ProxyPacket.Type, var now: Int) :
        ListProxy

    data class ListProxyItem(
        val name: String,
        val type: String,
        val delay: Long,
        val header: ListProxyHeader
    ) : ListProxy
}