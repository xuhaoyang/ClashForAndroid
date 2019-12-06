package com.github.kr328.clash.service

import android.net.LocalSocket
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.event.BandwidthEvent
import com.github.kr328.clash.core.event.LogEvent
import com.github.kr328.clash.core.event.SpeedEvent
import com.github.kr328.clash.core.utils.Log
import kotlin.concurrent.thread

class ClashEventPuller(private val clash: Clash, private val master: Master) {
    interface Master {
        fun onLogPulled(event: LogEvent)
        fun onSpeedPulled(event: SpeedEvent)
        fun onBandwidthPulled(event: BandwidthEvent)
    }

    private var logSocket: LocalSocket? = null
    private var speedSocket: LocalSocket? = null
    private var bandwidthSocket: LocalSocket? = null

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

    fun startSpeedPull() {
        synchronized(SpeedEvent::class) {
            if (speedSocket != null)
                return
        }

        thread {
            clash.pullSpeedEvent({
                synchronized(SpeedEvent::class) {
                    if (speedSocket != null) {
                        it.close()
                        return@pullSpeedEvent
                    } else {
                        speedSocket = it
                        Log.i("Speed puller started")
                    }

                }
            }) {
                master.onSpeedPulled(it)
            }

            synchronized(SpeedEvent::class) {
                Log.i("Speed puller stopped")

                speedSocket = null
            }
        }
    }

    fun startBandwidthPull() {
        synchronized(BandwidthEvent::class) {
            if (bandwidthSocket != null)
                return
        }

        thread {
            clash.pullBandwidthEvent({
                synchronized(BandwidthEvent::class) {
                    if (bandwidthSocket != null) {
                        it.close()
                        return@pullBandwidthEvent
                    } else {
                        bandwidthSocket = it
                        Log.i("Bandwidth puller started")
                    }

                }
            }) {
                master.onBandwidthPulled(it)
            }

            synchronized(BandwidthEvent::class) {
                Log.i("Bandwidth puller stopped")

                bandwidthSocket = null
            }
        }
    }

    fun stopLogPull() {
        synchronized(LogEvent::class) {
            logSocket.closeSilent()
        }
    }

    fun stopSpeedPull() {
        synchronized(SpeedEvent::class) {
            speedSocket.closeSilent()
        }
    }

    fun stopBandwidthPull() {
        synchronized(BandwidthEvent::class) {
            bandwidthSocket.closeSilent()
        }
    }

    private fun LocalSocket?.closeSilent() {
        runCatching {
            this?.outputStream?.write(0)
            this?.close()
        }
    }
}