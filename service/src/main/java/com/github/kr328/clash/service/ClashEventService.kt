package com.github.kr328.clash.service

import android.os.Handler
import android.os.Looper
import com.github.kr328.clash.core.event.*
import java.util.*
import kotlin.concurrent.thread

class ClashEventService(private val master: Master) : IClashEventService.Stub() {
    interface Master {
        fun requireEvent(event: Int)
        fun releaseEvent(event: Int)
    }

    companion object {
        private val EVENT_SET = setOf(Event.EVENT_LOG, Event.EVENT_TRAFFIC, Event.EVENT_PROXY_CHANGED)
    }

    private data class EventObserverRecord(val observer: IClashEventObserver, val acquiredEvent: MutableSet<Int>)

    private val observers = mutableMapOf<String, EventObserverRecord>()
    private lateinit var handler: Handler

    init {
        thread {
            Looper.prepare()

            handler = Handler()

            Looper.loop()
        }
    }

    fun preformProcessEvent(event: ProcessEvent) {
        handler.post {
            observers.values.forEach {
                it.observer.onProcessEvent(event)
            }
        }
    }

    fun preformLogEvent(event: LogEvent) {
        handler.post {
            observers.values.forEach {
                it.observer.onLogEvent(event)
            }
        }
    }

    fun preformProxyChangedEvent(event: ProxyChangedEvent) {
        handler.post {
            observers.values.forEach {
                it.observer.onProxyChangedEvent(event)
            }
        }
    }

    fun preformTrafficEvent(event: TrafficEvent) {
        handler.post {
            observers.values.forEach {
                it.observer.onTrafficEvent(event)
            }
        }
    }

    fun preformErrorEvent(event: ErrorEvent) {
        handler.post {
            observers.values.forEach {
                it.observer.onErrorEvent(event)
            }
        }
    }

    override fun acquireEvent(id: String?, event: Int) {
        handler.post {
            require(id != null)

            observers[id]?.acquiredEvent?.add(event)

            recastEventRequirement()
        }
    }

    override fun releaseEvent(id: String?, event: Int) {
        handler.post {
            require(id != null)

            observers[id]?.acquiredEvent?.remove(event)

            recastEventRequirement()
        }
    }

    override fun unregisterEventObserver(id: String?) {
        handler.post {
            require(id != null)

            observers.remove(id)

            recastEventRequirement()
        }
    }

    override fun registerEventObserver(id: String?, observer: IClashEventObserver?) {
        handler.post {
            require(id != null && observer != null)

            observers[id] = EventObserverRecord(observer, mutableSetOf())
        }
    }

    fun recastEventRequirement() {
        handler.post {
            val req = observers.values.flatMap {
                it.acquiredEvent
            }.toSet()
            val rel = EVENT_SET - req

            req.forEach(master::requireEvent)
            rel.forEach(master::releaseEvent)
        }
    }
}