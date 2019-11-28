package com.github.kr328.clash.model

data class ListProxyGroup(val name: String, val type: String, val proxies: List<ListProxy>, var now: ListProxy?) {
    data class ListProxy(val name: String, val type: String, val delay: Long)
}