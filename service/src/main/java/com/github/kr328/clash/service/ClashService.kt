package com.github.kr328.clash.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.util.Log
import androidx.lifecycle.Observer
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.ClashProcessStatus
import com.github.kr328.clash.service.data.ClashDatabase
import java.io.IOException
import java.util.concurrent.Executors

class ClashService : Service() {
    companion object {
        private val TAG = "ClashForAndroid"
    }

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var clash: Clash
    private var lastProfile: String? = null

    private var status = ClashProcessStatus(ClashProcessStatus.STATUS_STOPPED)
        set(value) {
            field = value

            for (observer in observers.values) {
                runCatching {
                    observer.onStatusChanged(status)
                }
            }
        }
    private val observers = mutableMapOf<String, IClashObserver>()

    private val defaultProfileObserver = object: Observer<String?> {
        override fun onChanged(url: String?) {
            executor.submit {
                if (url == null)
                    clash.stop()
                if (url == lastProfile)
                    return@submit
                try {
                    clash.loadProfile(Uri.parse(url))
                    lastProfile = url
                } catch (e: IOException) {
                    Log.w(TAG, "Load profile failure")

                    if (lastProfile == null) {
                        clash.stop()
                    } else {
                        val p = lastProfile
                        lastProfile = null
                        ClashDatabase.getInstance(this@ClashService)
                            .openClashProfileDao()
                            .setDefaultProfile(p ?: "")
                    }
                }
            }
        }
    }

    private inner class ClashServiceImpl : IClashService.Stub() {
        override fun registerObserver(
            id: String?,
            notifyCurrent: Boolean,
            observer: IClashObserver?
        ) {
            if (id == null || observer == null)
                throw RemoteException()

            observers[id] = observer

            if (notifyCurrent)
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

        override fun getClashProcessStatus(): ClashProcessStatus {
            return status
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

            when (it.status) {
                ClashProcessStatus.STATUS_STARTED ->
                    ClashDatabase.getInstance(this)
                        .openClashProfileDao()
                        .observeDefaultProfileUrl()
                        .observeForever(defaultProfileObserver)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return ClashServiceImpl()
    }

    override fun onDestroy() {
        clash.stop()
        executor.shutdown()

        ClashDatabase.getInstance(this)
            .openClashProfileDao()
            .observeDefaultProfileUrl()
            .removeObserver(defaultProfileObserver)

        super.onDestroy()
    }
}