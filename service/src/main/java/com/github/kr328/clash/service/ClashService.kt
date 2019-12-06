package com.github.kr328.clash.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.ClashProcess
import com.github.kr328.clash.core.event.*
import com.github.kr328.clash.core.model.GeneralPacket
import com.github.kr328.clash.core.model.ProxyPacket
import com.github.kr328.clash.core.utils.Log
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

class ClashService : Service(), IClashEventObserver, ClashEventService.Master,
    ClashProfileService.Master, ClashEventPuller.Master {
    private val executor = Executors.newSingleThreadExecutor()

    private val eventService = ClashEventService(this)
    private val profileService = ClashProfileService(this, this)
    private val settingService = ClashSettingService(this)

    private lateinit var clash: Clash
    private lateinit var puller: ClashEventPuller
    private lateinit var notification: ClashNotification

    private val clashService = object : IClashService.Stub() {
        override fun stopTunDevice() {
            notification.setVpn(false)

            clash.stopTunDevice()
        }

        override fun setSelectProxy(proxy: String?, selected: String?) {
            require(proxy != null && selected != null)

            try {
                clash.setSelectProxy(proxy, selected)

                this@ClashService.profileService.setCurrentProfileProxy(proxy, selected)
            } catch (e: IOException) {
                Log.w("Set proxy failure", e)

                this@ClashService.eventService.performErrorEvent(
                    ErrorEvent(ErrorEvent.Type.SET_PROXY_SELECTED, e.toString())
                )
            }
        }

        override fun queryGeneral(): GeneralPacket {
            return clash.queryGeneral()
        }

        override fun queryAllProxies(): ProxyPacket {
            return try {
                ProxyPacket.fromRawProxy(clash.queryProxies())
            } catch (e: Exception) {
                this@ClashService.eventService.performErrorEvent(
                    ErrorEvent(ErrorEvent.Type.QUERY_PROXY_FAILURE, e.toString())
                )
                ProxyPacket("Unknown", emptyMap())
            }
        }

        override fun start() {
            try {
                clash.process.start()
            } catch (e: Exception) {
                Log.e("Start failure", e)

                this@ClashService.eventService.performErrorEvent(
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
            } catch (e: Exception) {
                Log.e("Start tun failure", e)

                this@ClashService.eventService.performErrorEvent(
                    ErrorEvent(ErrorEvent.Type.START_FAILURE, e.toString())
                )
            } finally {
                fd.close()
            }
        }

        override fun getEventService(): IClashEventService {
            return this@ClashService.eventService
        }

        override fun getProfileService(): IClashProfileService {
            return this@ClashService.profileService
        }

        override fun getSettingService(): IClashSettingService {
            return this@ClashService.settingService
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
                        intArrayOf(Event.EVENT_SPEED)
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
            Event.EVENT_SPEED ->
                puller.startSpeedPull()
            Event.EVENT_LOG ->
                puller.startLogPuller()
            Event.EVENT_BANDWIDTH ->
                puller.startBandwidthPull()
        }

    }

    override fun releaseEvent(event: Int) {
        if (clash.process.getProcessStatus() == ProcessEvent.STOPPED)
            return

        when (event) {
            Event.EVENT_SPEED ->
                puller.stopSpeedPull()
            Event.EVENT_LOG ->
                puller.stopLogPull()
            Event.EVENT_BANDWIDTH ->
                puller.stopBandwidthPull()
        }
    }

    override fun onCreate() {
        super.onCreate()

        clash = Clash(
            this,
            filesDir.resolve("clash"),
            cacheDir.resolve("clash_controller"),
            eventService::performProcessEvent
        )

        puller = ClashEventPuller(clash, this)

        notification = ClashNotification(this)

        eventService.registerEventObserver(
            ClashService::class.java.name,
            this@ClashService,
            intArrayOf(Event.EVENT_SPEED)
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
                eventService.performSpeedEvent(SpeedEvent(0, 0))
                eventService.performBandwidthEvent(BandwidthEvent(0))

                notification.cancel()

                stopSelf()
            }
        }
    }

    private fun reloadProfile() {
        executor.submit {
            if ( clash.process.getProcessStatus() != ProcessEvent.STARTED)
                return@submit

            val active = profileService.queryActiveProfile()

            if (active == null) {
                clash.process.stop()
                return@submit
            }

            Log.i("Loading profile ${active.file}")

            try {
                val remove = clash.loadProfile(
                    File(active.file),
                    profileService.queryProfileSelected(active.id)
                )

                profileService.removeCurrentProfileProxy(remove)

                notification.setProfile(active.name)

                eventService.performProfileReloadEvent(ProfileReloadEvent())
            } catch (e: Exception) {
                clash.process.stop()
                Log.w("Load profile failure", e)
            }
        }
    }

    override fun onSpeedEvent(event: SpeedEvent?) {
        notification.setSpeed(event?.up ?: 0, event?.down ?: 0)
    }

    override fun preformProfileChanged() {
        eventService.performProfileChangedEvent(ProfileChangedEvent())
    }

    override fun onLogPulled(event: LogEvent) {
        eventService.performLogEvent(event)
    }

    override fun onSpeedPulled(event: SpeedEvent) {
        eventService.performSpeedEvent(event)
    }

    override fun onBandwidthPulled(event: BandwidthEvent) {
        eventService.performBandwidthEvent(event)
    }

    override fun onBandwidthEvent(event: BandwidthEvent?) {}
    override fun onLogEvent(event: LogEvent?) {}
    override fun onErrorEvent(event: ErrorEvent?) {}
    override fun onProfileReloaded(event: ProfileReloadEvent?) {}
    override fun asBinder(): IBinder = object : Binder() {
        override fun queryLocalInterface(descriptor: String): IInterface? {
            return this@ClashService
        }
    }
}