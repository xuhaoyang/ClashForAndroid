package com.github.kr328.clash.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.*
import com.github.kr328.clash.core.event.*
import com.github.kr328.clash.service.net.DefaultNetworkObserver

class TunService : VpnService(), IClashEventObserver {
    companion object {
        const val TAG = "ClashForAndroid"

        val DEFAULT_DNS = listOf(
            "1.1.1.1",
            "8.8.8.8",
            "208.67.222.222",
            "114.114.114.114",
            "223.5.5.5",
            "119.29.29.29"
        )

        // from https://github.com/shadowsocks/shadowsocks-android/blob/master/core/src/main/java/com/github/shadowsocks/bg/VpnService.kt
        private const val VPN_MTU = 1500
        private const val PRIVATE_VLAN4_CLIENT = "172.19.0.1"
        private const val PRIVATE_VLAN6_CLIENT = "fdfe:dcba:9876::1"
    }

    private lateinit var fileDescriptor: ParcelFileDescriptor
    private lateinit var clash: IClashService
    private lateinit var defaultNetworkObserver: DefaultNetworkObserver
    private val connection = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            stopSelf()
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val clash = IClashService.Stub.asInterface(
                service
            ) ?: throw NullPointerException()

            this@TunService.clash = clash

            clash.eventService.registerEventObserver(TunService::class.java.simpleName,
                this@TunService,
                intArrayOf())

            if ( clash.currentProcessStatus == ProcessEvent.STARTED )
                clash.startTunDevice(fileDescriptor, VPN_MTU)
            else
                clash.start()
        }
    }


    override fun onCreate() {
        super.onCreate()

        if ( prepare(this) != null ) {
            stopSelf()
            return
        }

        fileDescriptor = Builder()
            .addAddress(PRIVATE_VLAN4_CLIENT, 30)
            .addAddress(PRIVATE_VLAN6_CLIENT, 126)
            .addDefaultDns()
            .addDisallowedApplication(packageName)
            .addBypassPrivateRoute()
            .setMtu(VPN_MTU)
            .setBlocking(false)
            .setMeteredCompat(false)
            .establish() ?: throw NullPointerException("Unable to establish VPN")

        bindService(Intent(this, ClashService::class.java), connection, Context.BIND_AUTO_CREATE)

        defaultNetworkObserver = DefaultNetworkObserver(this) {
            setUnderlyingNetworks(it?.run { arrayOf(this) })
        }

        defaultNetworkObserver.register()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        fileDescriptor.close()

        clash.stopTunDevice()
        clash.stop()

        unbindService(connection)

        defaultNetworkObserver.unregister()
    }

    override fun onProcessEvent(event: ProcessEvent?) {
        when ( event ) {
            ProcessEvent.STOPPED ->
                stopSelf()
            ProcessEvent.STARTED ->
                clash.startTunDevice(fileDescriptor, VPN_MTU)
        }
    }

    private fun Builder.setMeteredCompat(isMetered: Boolean): Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            setMetered(isMetered)
        return this
    }

    private fun Builder.addBypassPrivateRoute(): Builder {
        // IPv4
        resources.getStringArray(R.array.bypass_private_route).forEach {
            val address = it.split("/")
            addRoute(address[0], address[1].toInt())
        }

        // IPv6
        addRoute("::", 0)

        return this
    }

    private fun Builder.addDefaultDns(): Builder {
        resources.getStringArray(R.array.default_dns).forEach {
            addDnsServer(it)
        }
        return this
    }

    override fun onLogEvent(event: LogEvent?) {}
    override fun onErrorEvent(event: ErrorEvent?) {}
    override fun onProfileChanged(event: ProfileChangedEvent?) {}
    override fun onProxyChangedEvent(event: ProxyChangedEvent?) {}
    override fun onTrafficEvent(event: TrafficEvent?) {}
    override fun asBinder(): IBinder = object: Binder() {
        override fun queryLocalInterface(descriptor: String): IInterface? {
            return this@TunService
        }
    }
}