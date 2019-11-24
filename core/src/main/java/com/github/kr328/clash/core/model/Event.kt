package com.github.kr328.clash.core.model

interface Event {
    companion object {
        const val EVENT_LOG = 1
        const val EVENT_PROXY_CHANGED = 2
    }
}