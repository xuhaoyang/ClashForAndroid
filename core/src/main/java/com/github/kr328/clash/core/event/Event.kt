package com.github.kr328.clash.core.event

interface Event {
    companion object {
        const val EVENT_CLOSE = 0
        const val EVENT_LOG = 1
        const val EVENT_PROXY_CHANGED = 2
        const val EVENT_TRAFFIC = 3
    }
}