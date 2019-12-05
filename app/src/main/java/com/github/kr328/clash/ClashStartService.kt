package com.github.kr328.clash

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.kr328.clash.utils.ServiceUtils
import kotlin.concurrent.thread

class ClashStartService : Service() {
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "clash_start_service_notification_channel"
        private const val NOTIFICATION_ID = 142
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NotificationManagerCompat.from(this).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        getString(R.string.clash_start_service_notification_channel),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
        }

        val notification = NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.clash_start_service_notification))
            .setSmallIcon(R.drawable.ic_notification_icon)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        thread {
            ServiceUtils.startProxyService(this)

            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return Binder()
    }
}