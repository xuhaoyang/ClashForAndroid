package com.github.kr328.clash

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.ClashProcessStatus
import java.io.IOException

class ClashService : Service() {
    companion object {
        private val TAG = "ClashForAndroid"
    }

    private lateinit var clash: Clash

    private var status = ClashProcessStatus(ClashProcessStatus.STATUS_STOPPED)
        set(value) {
            field = value

            for (kv in observers) {
                runCatching {
                    kv.value.onStatusChanged(status)
                }
            }
        }
    private val observers = mutableMapOf<String, IClashObserver>()

    private inner class ClashServiceImpl : IClashService.Stub() {
        override fun registerObserver(id: String?, notifyCurrent: Boolean, observer: IClashObserver?) {
            if (id == null || observer == null)
                throw RemoteException()

            observers[id] = observer

            if ( notifyCurrent )
                observer.onStatusChanged(status)


            observer.asBinder().linkToDeath({
                observers.remove(id)
            }, 0)
        }

        override fun unregisterObserver(id: String?) {
            if (id == null)
                throw RemoteException()

            observers.remove(id)
        }

        override fun stopTunDevice() {
            clash.stop()
        }

        override fun start() {
            try {
                clash.start()
            } catch (e: IOException) {
                throw RemoteException(e.message)
            }
        }

        override fun stop() {
            clash.stop()
        }

        override fun startTunDevice(fd: ParcelFileDescriptor, mtu: Int) {
            try {
                clash.startTunDevice(fd.fileDescriptor, mtu)
            } catch (e: IOException) {
                throw RemoteException(e.message)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        clash = Clash(
            this,
            filesDir.resolve("clash"),
            cacheDir.resolve("clash_controller")
        ) {
            this.status = it
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return ClashServiceImpl()
    }

    override fun onDestroy() {
        clash.stop()

        super.onDestroy()
    }
}