package com.github.kr328.clash.service

import android.os.Handler
import android.os.Looper
import com.github.kr328.clash.core.event.*
import kotlin.concurrent.thread

class ClashEventService(private val master: Master) : IClashEventService.Stub() {
    interface Master {
        fun requireEvent(event: Int)
        fun releaseEvent(event: Int)
    }

    companion object {
        private val EVENT_SET = setOf(Event.EVENT_LOG, Event.EVENT_TRAFFIC, Event.EVENT_PROXY_CHANGED)
    }

    private data class EventObserverRecord(val observer: IClashEventObserver, val acquiredEvent: Set<Int>)

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
                if ( it.acquiredEvent.contains(Event.EVENT_LOG) )
                    it.observer.onLogEvent(event)
            }
        }
    }

    fun preformProxyChangedEvent(event: ProxyChangedEvent) {
        handler.post {
            observers.values.forEach {
                if ( it.acquiredEvent.contains(Event.EVENT_PROXY_CHANGED) )
                    it.observer.onProxyChangedEvent(event)
            }
        }
    }

    fun preformTrafficEvent(event: TrafficEvent) {
        handler.post {
            observers.values.forEach {
                if ( it.acquiredEvent.contains(Event.EVENT_TRAFFIC) )
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

    fun preformProfileChangedEvent(event: ProfileChangedEvent) {
        handler.post {
            observers.values.forEach {
                it.observer.onProfileChanged(event)
            }
        }
    }

    override fun unregisterEventObserver(id: String?) {
        handler.post {
            require(id != null)

            observers.remove(id)

            recastEventRequirement()
        }
    }

    override fun registerEventObserver(
        id: String?,
        observer: IClashEventObserver?,
        events: IntArray?
    ) {
        handler.post {
            require(id != null && observer != null && events != null)

            observers[id] = EventObserverRecord(observer, events.toSet())

            observer.asBinder().linkToDeath({
                unregisterEventObserver(id)
            }, 0)

            recastEventRequirement()
        }
    }

    private fun recastEventRequirement() {
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