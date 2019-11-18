package com.github.kr328.clash

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.IBinder
import android.os.ParcelFileDescriptor
import java.lang.RuntimeException

class TunService : VpnService() {
    private lateinit var fileDescriptor: ParcelFileDescriptor
    private val connection = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            stopSelf()
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            IClashService.Stub.asInterface(service)?.startTunDevice(fileDescriptor.fd, 1500) ?: stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        fileDescriptor = Builder()
            .addAddress("10.0.0.1", 24)
            .addDisallowedApplication(packageName)
            .addRoute("0.0.0.0", 0)
            .setMtu(1500)
            .setBlocking(false)
            .establish() ?: throw RuntimeException("Unable to establish VPN")

        bindService(Intent(this, ClashService::class.java), connection, Context.BIND_AUTO_CREATE)

        return Service.START_STICKY
    }

    override fun onDestroy() {
        fileDescriptor.close()
        unbindService(connection)

        super.onDestroy()
    }
}