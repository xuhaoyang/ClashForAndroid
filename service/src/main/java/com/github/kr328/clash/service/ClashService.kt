package com.github.kr328.clash.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.ParcelFileDescriptor
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.event.*
import com.github.kr328.clash.core.utils.Log
import com.github.kr328.clash.service.data.ClashDatabase
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class ClashService : Service(), IClashEventObserver, ClashEventService.Master,
    ClashProfileService.Master {
    private val executor = Executors.newSingleThreadExecutor()
    private var pollThread: Thread? = null

    private val eventService = ClashEventService(this)
    private val profileService = ClashProfileService(this, this)

    private lateinit var clash: Clash
    private lateinit var database: ClashDatabase

    private val clashService = object : IClashService.Stub() {
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

        override fun getProfileService(): IClashProfileService {
            return this@ClashService.profileService
        }

        override fun getCurrentProcessStatus(): ProcessEvent {
            return clash.process.getProcessStatus()
        }
    }

    override fun requireEvent(event: Int) {
        if (clash.process.getProcessStatus() == ProcessEvent.STARTED)
            clash.setEventEnabled(event, true)
    }

    override fun releaseEvent(event: Int) {
        if (clash.process.getProcessStatus() == ProcessEvent.STARTED)
            clash.setEventEnabled(event, false)
    }

    override fun onCreate() {
        super.onCreate()

        database = ClashDatabase.getInstance(this)

        clash = Clash(
            this,
            filesDir.resolve("clash"),
            cacheDir.resolve("clash_controller"),
            eventService::preformProcessEvent
        )

        eventService.registerEventObserver(
            ClashService::class.java.simpleName,
            this,
            intArrayOf(Event.EVENT_TRAFFIC)
        )
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
        reloadProfile()
    }

    override fun onProcessEvent(event: ProcessEvent?) {
        when (event!!) {
            ProcessEvent.STARTED -> {
                pollThread = thread {
                    try {
                        clash.pollEvent {
                            when (it) {
                                is LogEvent ->
                                    eventService.preformLogEvent(it)
                                is TrafficEvent ->
                                    eventService.preformTrafficEvent(it)
                                is ProxyChangedEvent ->
                                    eventService.preformProxyChangedEvent(it)
                            }
                        }
                    }
                    catch (e: Exception) {
                        Log.i("Event poll exited", e)
                    }
                }

                reloadProfile()
            }
            ProcessEvent.STOPPED -> {
                pollThread?.interrupt()
                pollThread = null
            }
        }
    }

    private fun reloadProfile() {
        executor.submit {
            val active = database.openClashProfileDao().queryActiveProfile()

            if (active == null) {
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

    override fun onTrafficEvent(event: TrafficEvent?) {

    }

    override fun preformProfileChanged() {
        eventService.preformProfileChangedEvent(ProfileChangedEvent())
    }

    override fun onProxyChangedEvent(event: ProxyChangedEvent?) {}
    override fun onLogEvent(event: LogEvent?) {}
    override fun onErrorEvent(event: ErrorEvent?) {}
    override fun asBinder(): IBinder = object : Binder() {
        override fun queryLocalInterface(descriptor: String): IInterface? {
            return this@ClashService
        }
    }
}