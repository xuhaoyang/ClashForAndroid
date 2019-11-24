package com.github.kr328.clash.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

class ClashNotification(private val context: Context) {
    companion object {
        private const val CLASH_STATUS_NOTIFICATION_CHANNEL = "clash_status_channel"
        private const val CLASH_STATUS_NOTIFICATION_ID = 413

        private const val MAIN_ACTIVITY_NAME = ".MainActivity"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManagerCompat.from(context)
                .createNotificationChannel(
                    NotificationChannel(CLASH_STATUS_NOTIFICATION_CHANNEL,
                        context.getString(R.string.clash_service_status_channel),
                        NotificationManager.IMPORTANCE_MIN)
                )
        }
    }

    fun show() {

    }

    fun cancel() {

    }
}