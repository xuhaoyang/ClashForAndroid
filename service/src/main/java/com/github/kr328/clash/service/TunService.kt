package com.github.kr328.clash.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import com.github.kr328.clash.core.ClashProcessStatus

class TunService : VpnService() {
    companion object {
        val TAG = "ClashForAndroid"

        // from https://github.com/shadowsocks/shadowsocks-android/blob/master/core/src/main/java/com/github/shadowsocks/bg/VpnService.kt
        private const val VPN_MTU = 1500
        private const val PRIVATE_VLAN4_CLIENT = "172.19.0.1"
        private const val PRIVATE_VLAN4_ROUTER = "172.19.0.2"
        private const val PRIVATE_VLAN6_CLIENT = "fdfe:dcba:9876::1"
        private const val PRIVATE_VLAN6_ROUTER = "fdfe:dcba:9876::2"
    }

    private var fileDescriptor: ParcelFileDescriptor? = null
    private var clash: IClashService? = null
    private val connection = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            clash = null
            stopSelf()
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val clash = IClashService.Stub.asInterface(
                service
            ) ?: throw NullPointerException()


            clash.registerObserver("tun", false, object: IClashObserver.Stub() {
                override fun onStatusChanged(status: ClashProcessStatus?) {
                    if ( status == null )
                        return

                    Log.d(TAG, "New clash status $status")

                    when ( status ) {
                        ClashProcessStatus.STATUS_STOPPED ->
                            stopSelf()
                        ClashProcessStatus.STATUS_STARTED ->
                            clash.startTunDevice(fileDescriptor, VPN_MTU)
                    }
                }
            })

            if ( clash.clashProcessStatus == ClashProcessStatus.STATUS_STARTED )
                clash.startTunDevice(fileDescriptor, VPN_MTU)
            else
                clash.start()

            this@TunService.clash = clash
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
            .addDnsServer(PRIVATE_VLAN4_ROUTER)
            .addAddress(PRIVATE_VLAN6_CLIENT, 126)
            .addDnsServer(PRIVATE_VLAN6_ROUTER)
            .addDisallowedApplication(packageName)
            .addRoute("0.0.0.0", 0)
            .addRoute("::", 0)
            .setMtu(VPN_MTU)
            .setBlocking(false)
            .establish() ?: throw NullPointerException("Unable to establish VPN")

        bindService(Intent(this, ClashService::class.java), connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        fileDescriptor?.close()

        clash?.stopTunDevice()
        clash?.stop()

        unbindService(connection)

        super.onDestroy()
    }
}