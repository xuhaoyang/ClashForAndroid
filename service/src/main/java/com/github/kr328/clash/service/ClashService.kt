package com.github.kr328.clash.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.util.Log
import androidx.lifecycle.Observer
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.ClashProcessStatus
import com.github.kr328.clash.service.data.ClashDatabase
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

class ClashService : Service() {
    companion object {
        private val TAG = "ClashForAndroid"
    }

    private val handler = Handler()
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var clash: Clash

    private var status = ClashProcessStatus(ClashProcessStatus.STATUS_STOPPED_INT)
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
        override fun onChanged(file: String?) {
            executor.submit {
                Log.i(TAG, "Loading profile $file")

                if (file == null)
                    return@submit

                try {
                    clash.loadProfile(File(file))
                } catch (e: IOException) {
                    Log.w(TAG, "Load profile failure")
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
                Log.e(TAG, "Start failure", e)

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
                fd.close()
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

            handler.post {
                when (it.status) {
                    ClashProcessStatus.STATUS_STARTED_INT ->
                        ClashDatabase.getInstance(this)
                            .openClashProfileDao()
                            .observeDefaultProfileCache()
                            .observeForever(defaultProfileObserver)
                    ClashProcessStatus.STATUS_STOPPED_INT ->
                        ClashDatabase.getInstance(this)
                            .openClashProfileDao()
                            .observeDefaultProfileCache()
                            .removeObserver(defaultProfileObserver)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        clash.start()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return ClashServiceImpl()
    }

    override fun onDestroy() {
        clash.stop()
        executor.shutdown()

        super.onDestroy()
    }
}