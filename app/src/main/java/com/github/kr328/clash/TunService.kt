package com.github.kr328.clash

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.IBinder
import android.os.ParcelFileDescriptor
import com.github.kr328.clash.core.ClashProcess
import com.github.kr328.clash.model.ClashStatus
import java.lang.NullPointerException
import java.lang.RuntimeException

class TunService : VpnService() {
    companion object {
        // from https://github.com/shadowsocks/shadowsocks-android/blob/master/core/src/main/java/com/github/shadowsocks/bg/VpnService.kt
        private const val VPN_MTU = 1500
        private const val PRIVATE_VLAN4_CLIENT = "172.19.0.1"
        private const val PRIVATE_VLAN4_ROUTER = "172.19.0.2"
        private const val PRIVATE_VLAN6_CLIENT = "fdfe:dcba:9876::1"
        private const val PRIVATE_VLAN6_ROUTER = "fdfe:dcba:9876::2"
    }

    private lateinit var fileDescriptor: ParcelFileDescriptor
    private val connection = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            stopSelf()
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val clash = IClashService.Stub.asInterface(service) ?: throw NullPointerException()

            clash.start()
            clash.registerObserver("tun", false, object: IClashObserver.Stub() {
                override fun onStatusChanged(status: ClashStatus?) {
                    if ( status == null )
                        return

                    when ( status.status ) {
                        ClashProcess.Status.STOPPED -> stopSelf()
                        ClashProcess.Status.STARTED -> clash.startTunDevice(fileDescriptor, VPN_MTU)
                    }
                }
            })
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

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

        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        fileDescriptor.close()

        unbindService(connection)

        super.onDestroy()
    }
}