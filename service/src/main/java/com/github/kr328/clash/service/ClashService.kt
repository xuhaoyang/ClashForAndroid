package com.github.kr328.clash.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

class ClashService : Service(), IClashEventObserver, ClashEventService.Master,
    ClashProfileService.Master, ClashEventPuller.Master {
    private val executor = Executors.newSingleThreadExecutor()

    private val eventService = ClashEventService(this)
    private val profileService = ClashProfileService(this, this)

    private lateinit var clash: Clash
    private lateinit var puller: ClashEventPuller
    private lateinit var database: ClashDatabase
    private lateinit var notification: ClashNotification

    private val clashService = object : IClashService.Stub() {
        override fun stopTunDevice() {
            notification.setVpn(false)

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
                notification.setVpn(true)

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

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON ->
                    eventService.registerEventObserver(
                        ClashService::class.java.name,
                        this@ClashService,
                        intArrayOf(Event.EVENT_TRAFFIC)
                    )
                Intent.ACTION_SCREEN_OFF ->
                    eventService.registerEventObserver(
                        ClashService::class.java.name,
                        this@ClashService,
                        intArrayOf()
                    )
            }
        }
    }

    override fun requireEvent(event: Int) {
        if (clash.process.getProcessStatus() == ProcessEvent.STOPPED)
            return

        when (event) {
            Event.EVENT_TRAFFIC ->
                puller.startTrafficPull()
            Event.EVENT_LOG ->
                puller.startLogPuller()
        }
    }

    override fun releaseEvent(event: Int) {
        if (clash.process.getProcessStatus() == ProcessEvent.STOPPED)
            return

        when (event) {
            Event.EVENT_TRAFFIC ->
                puller.stopTrafficPull()
            Event.EVENT_LOG ->
                puller.stopLogPull()
        }
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

        puller = ClashEventPuller(clash, this)

        notification = ClashNotification(this)

        eventService.registerEventObserver(
            ClashService::class.java.name,
            this@ClashService,
            intArrayOf(Event.EVENT_TRAFFIC)
        )

        registerReceiver(screenReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })
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
        eventService.shutdown()

        unregisterReceiver(screenReceiver)

        super.onDestroy()
    }

    override fun onProfileChanged(event: ProfileChangedEvent?) {
        reloadProfile()
    }

    override fun onProcessEvent(event: ProcessEvent?) {
        when (event!!) {
            ProcessEvent.STARTED -> {
                reloadProfile()

                notification.show()

                eventService.recastEventRequirement()
            }
            ProcessEvent.STOPPED -> {
                eventService.preformTrafficEvent(TrafficEvent(0, 0, 0))

                notification.cancel()
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

                notification.setProfile(active.name)
            } catch (e: IOException) {
                clash.process.stop()
                Log.w("Load profile failure", e)
            }
        }
    }

    override fun onTrafficEvent(event: TrafficEvent?) {
        notification.setSpeed(event?.up ?: 0, event?.down ?: 0)
    }

    override fun preformProfileChanged() {
        eventService.preformProfileChangedEvent(ProfileChangedEvent())
    }

    override fun onLogPulled(event: LogEvent) {
        eventService.preformLogEvent(event)
    }

    override fun onTrafficPulled(event: TrafficEvent) {
        eventService.preformTrafficEvent(event)
    }

    override fun onProxyChangedPulled(event: ProxyChangedEvent) {
        eventService.preformProxyChangedEvent(event)
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