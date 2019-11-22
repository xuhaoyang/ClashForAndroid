package com.github.kr328.clash.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.ClashProcessStatus
import com.github.kr328.clash.service.data.ClashDatabase
import com.github.kr328.clash.service.data.ClashProfileEntity
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

class ClashService : Service() {
    companion object {
        private val TAG = "ClashForAndroid"

        private const val CLASH_STATUS_NOTIFICATION_CHANNEL = "clash_status_channel"
        private const val CLASH_STATUS_NOTIFICATION_ID = 413

        private const val MAIN_ACTIVITY_NAME = ".MainActivity"
    }

    private val handler = Handler()
    private val executor = Executors.newSingleThreadExecutor()

    private lateinit var clash: Clash
    private lateinit var currentProfile: LiveData<ClashProfileEntity?>

    private var tunEnabled = false

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

    private val defaultProfileObserver = object: Observer<ClashProfileEntity?> {
        override fun onChanged(file: ClashProfileEntity?) {
            executor.submit {
                if (file == null){
                    clash.stop()
                    return@submit
                }

                Log.i(TAG, "Loading profile ${file.cache}")

                try {
                    clash.loadProfile(File(file.cache))
                } catch (e: IOException) {
                    clash.stop()
                    Log.w(TAG, "Load profile failure", e)
                }

                handler.post(this@ClashService::updateNotification)
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

            handler.post(this@ClashService::updateNotification)
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
                tunEnabled = true
            } catch (e: IOException) {
                throw RemoteException(e.message)
            }

            handler.post(this@ClashService::updateNotification)
        }
    }

    override fun onCreate() {
        super.onCreate()

        currentProfile = ClashDatabase.getInstance(this)
            .openClashProfileDao()
            .observeSelectedProfile()

        clash = Clash(
            this,
            filesDir.resolve("clash"),
            cacheDir.resolve("clash_controller")
        ) {
            this.status = it

            handler.post {
                when (it.status) {
                    ClashProcessStatus.STATUS_STARTED_INT ->
                        currentProfile.observeForever(defaultProfileObserver)
                    ClashProcessStatus.STATUS_STOPPED_INT ->
                        currentProfile.removeObserver(defaultProfileObserver)
                }
            }

            handler.post(this::updateNotification)
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

    private fun updateNotification() {
        if ( status == ClashProcessStatus.STATUS_STARTED ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManagerCompat.from(this)
                    .createNotificationChannel(NotificationChannel(CLASH_STATUS_NOTIFICATION_CHANNEL,
                        getString(R.string.clash_service_status_channel),
                        NotificationManager.IMPORTANCE_MIN))
            }

            val notification = NotificationCompat.Builder(this, CLASH_STATUS_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setMode()
                .setOngoing(true)
                .setColorized(true)
                .setColor(getColor(R.color.colorAccentService))
                .setShowWhen(false)
                .setContentTitle(getString(R.string.clash_service_running))
                .setContentText(getString(R.string.clash_service_running_message, currentProfile.value?.name))
                .setContentIntent(PendingIntent.getActivity(
                    this,
                    CLASH_STATUS_NOTIFICATION_ID,
                    Intent.makeMainActivity(ComponentName.createRelative(packageName, MAIN_ACTIVITY_NAME))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    PendingIntent.FLAG_UPDATE_CURRENT
                ))
                .build()

            NotificationManagerCompat.from(this).notify(CLASH_STATUS_NOTIFICATION_ID, notification)
        }
        else {
            NotificationManagerCompat.from(this).cancel(CLASH_STATUS_NOTIFICATION_ID)
        }
    }

    private fun NotificationCompat.Builder.setMode(): NotificationCompat.Builder {
        if ( tunEnabled )
            setSubText(getString(R.string.clash_service_vpn_mode))
        return this
    }
}