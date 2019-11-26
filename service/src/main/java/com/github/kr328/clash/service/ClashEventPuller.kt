package com.github.kr328.clash.service

import android.net.LocalSocket
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.event.LogEvent
import com.github.kr328.clash.core.event.ProxyChangedEvent
import com.github.kr328.clash.core.event.TrafficEvent
import com.github.kr328.clash.core.utils.Log
import kotlin.concurrent.thread

class ClashEventPuller(private val clash: Clash, private val master: Master) {
    interface Master {
        fun onLogPulled(event: LogEvent)
        fun onTrafficPulled(event: TrafficEvent)
        fun onProxyChangedPulled(event: ProxyChangedEvent)
    }

    private var logSocket: LocalSocket? = null
    private var trafficSocket: LocalSocket? = null
    private var proxyChangedSocket: LocalSocket? = null

    fun startLogPuller() {
        synchronized(LogEvent::class) {
            if (logSocket != null)
                return
        }

        thread {
            clash.pullLogsEvent({
                synchronized(LogEvent::class) {
                    if (logSocket != null)
                        it.close()
                    else
                        logSocket = it

                    Log.i("log puller started")
                }
            }) {
                master.onLogPulled(it)
            }

            synchronized(LogEvent::class) {
                Log.i("log puller stopped")

                logSocket = null
            }
        }
    }

    fun stopLogPull() {
        synchronized(LogEvent::class) {
            logSocket.closeSilent()
        }
    }

    fun startTrafficPull() {
        synchronized(TrafficEvent::class) {
            if (trafficSocket != null)
                return
        }

        thread {
            clash.pullTrafficEvent({
                synchronized(TrafficEvent::class) {
                    if (trafficSocket != null) {
                        it.close()
                        return@pullTrafficEvent
                    } else {
                        trafficSocket = it
                        Log.i("Traffic puller started")
                    }

                }
            }) {
                master.onTrafficPulled(it)
            }

            synchronized(TrafficEvent::class) {
                Log.i("Traffic puller stopped")

                trafficSocket = null
            }
        }
    }

    fun stopTrafficPull() {
        synchronized(TrafficEvent::class) {
            trafficSocket.closeSilent()
        }
    }

    private fun LocalSocket?.closeSilent() {
        runCatching {
            this?.close()
        }
    }
}