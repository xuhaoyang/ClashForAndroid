package com.github.kr328.clash.service

import android.app.*
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Handler
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.kr328.clash.core.utils.ByteFormatter

class ClashNotification(private val context: Service) {
    companion object {
        private const val CLASH_STATUS_NOTIFICATION_CHANNEL = "clash_status_channel"
        private const val CLASH_STATUS_NOTIFICATION_ID = 413

        private const val MAIN_ACTIVITY_NAME = ".MainActivity"
    }

    private val handler = Handler()
    private var showing = false

    private val baseBuilder = NotificationCompat.Builder(context, CLASH_STATUS_NOTIFICATION_CHANNEL)
        .setSmallIcon(R.drawable.ic_notification_icon)
        .setOngoing(true)
        .setColor(context.getColor(R.color.colorAccentService))
        .setColorized(true)
        .setShowWhen(false)
        .setContentIntent(
            PendingIntent.getActivity(
                context,
                CLASH_STATUS_NOTIFICATION_ID,
                Intent().setComponent(
                    ComponentName.createRelative(
                        context,
                        MAIN_ACTIVITY_NAME
                    )
                ),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

    private var vpn = false
    private var up = 0L
    private var down = 0L
    private var profile = "None"

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManagerCompat.from(context)
                .createNotificationChannel(
                    NotificationChannel(
                        CLASH_STATUS_NOTIFICATION_CHANNEL,
                        context.getString(R.string.clash_service_status_channel),
                        NotificationManager.IMPORTANCE_MIN
                    )
                )
        }
    }

    fun show() {
        handler.post {
            showing = true

            update()
        }
    }

    fun cancel() {
        handler.post {
            if (showing)
                context.stopForeground(true)

            showing = false
        }
    }

    fun setProfile(profile: String) {
        handler.post {
            this.profile = profile

            update()
        }
    }

    fun setSpeed(up: Long, down: Long) {
        handler.post {
            this.up = up
            this.down = down

            update()
        }
    }

    fun setVpn(vpn: Boolean) {
        handler.post {
            this.vpn = vpn

            update()
        }
    }

    private fun update() {
        if (showing)
            context.startForeground(CLASH_STATUS_NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        return baseBuilder
            .setContentTitle(profile)
            .setContentText(
                context.getString(
                    R.string.clash_notification_content,
                    ByteFormatter.byteToStringSecond(up),
                    ByteFormatter.byteToStringSecond(down)
                )
            )
            .setSubText(if (vpn) context.getText(R.string.clash_service_vpn_mode) else null)
            .build()
    }
}