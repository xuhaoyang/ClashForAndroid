package com.github.kr328.clash.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.ParcelFileDescriptor
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.event.*
import com.github.kr328.clash.core.utils.Log
import com.github.kr328.clash.service.data.ClashDatabase
import java.io.File
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class ClashService : Service(), IClashEventObserver, ClashEventService.Master {
    private val executor = Executors.newSingleThreadExecutor()
    private var pollThread: Thread? = null

    private val eventService = ClashEventService(this)

    private lateinit var clash: Clash
    private lateinit var database: ClashDatabase

    private val clashService = object: IClashService.Stub() {
        override fun stopTunDevice() {
            clash.stopTunDevice()
        }

        override fun start() {
            try {
                clash.process.start()
            } catch (e: IOException) {
                Log.e("Start failure", e)

                this@ClashService.eventService.preformErrorEvent(
                    ErrorEvent(ErrorEvent.Type.START_FAILURE, e.toString())
                )
            }
        }

        override fun stop() {
            clash.process.stop()
        }

        override fun startTunDevice(fd: ParcelFileDescriptor, mtu: Int) {
            try {
                clash.startTunDevice(fd.fileDescriptor, mtu)
                fd.close()
            } catch (e: IOException) {
                Log.e("Start tun failure", e)

                this@ClashService.eventService.preformErrorEvent(
                    ErrorEvent(ErrorEvent.Type.START_FAILURE, e.toString())
                )
            }
        }

        override fun getEventService(): IClashEventService {
            return this@ClashService.eventService
        }

        override fun getCurrentProcessStatus(): ProcessEvent {
            return clash.process.getProcessStatus()
        }
    }

    override fun requireEvent(event: Int) {
        clash.setEventEnabled(event, true)
    }

    override fun releaseEvent(event: Int) {
        clash.setEventEnabled(event, false)
    }

    override fun onCreate() {
        super.onCreate()

        database = ClashDatabase.getInstance(this)

        eventService.registerEventObserver(ClashService::class.java.simpleName,
            this,
            intArrayOf(Event.EVENT_TRAFFIC))

        clash = Clash(
            this,
            filesDir.resolve("clash"),
            cacheDir.resolve("clash_controller"),
            eventService::preformProcessEvent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        clash.process.start()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return clashService
    }

    override fun onDestroy() {
        clash.process.stop()
        executor.shutdown()

        super.onDestroy()
    }

    override fun onProfileChanged(event: ProfileChangedEvent?) {
        executor.submit {
            val active = database.openClashProfileDao().queryActiveProfile()

            if (active == null){
                clash.process.stop()
                return@submit
            }

            Log.i("Loading profile ${active.cache}")

            try {
                clash.loadProfile(File(active.cache))
            } catch (e: IOException) {
                clash.process.stop()
                Log.w("Load profile failure", e)
            }
        }
    }

    override fun onProcessEvent(event: ProcessEvent?) {
        when ( event!! ) {
            ProcessEvent.STARTED -> {
                pollThread = thread {
                    clash.pollEvent {
                        try {
                            when ( it ) {
                                is LogEvent ->
                                    eventService.preformLogEvent(it)
                                is TrafficEvent ->
                                    eventService.preformTrafficEvent(it)
                                is ProxyChangedEvent ->
                                    eventService.preformProxyChangedEvent(it)
                            }
                        }
                        catch (e: Exception) {}
                    }
                }
            }
            ProcessEvent.STOPPED -> {
                pollThread?.interrupt()
                pollThread = null
            }
        }
    }

    override fun onTrafficEvent(event: TrafficEvent?) {

    }

    override fun onProxyChangedEvent(event: ProxyChangedEvent?) {}
    override fun onLogEvent(event: LogEvent?) {}
    override fun onErrorEvent(event: ErrorEvent?) {}
    override fun asBinder(): IBinder = throw IllegalArgumentException("asBinder Unsupported")
}