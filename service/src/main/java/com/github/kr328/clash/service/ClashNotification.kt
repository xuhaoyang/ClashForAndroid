package com.github.kr328.clash.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.kr328.clash.core.utils.ByteFormatter
import com.github.kr328.clash.core.utils.Log

class ClashNotification(private val context: Context) {
    companion object {
        private const val CLASH_STATUS_NOTIFICATION_CHANNEL = "clash_status_channel"
        private const val CLASH_STATUS_NOTIFICATION_ID = 413

        private const val MAIN_ACTIVITY_NAME = ".MainActivity"
    }

    private val handler = Handler()
    private var remoteViews: RemoteViews? = null
    private var notification: Notification? = null

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
        handler.post {
            if ( notification != null )
                return@post

            remoteViews = RemoteViews(context.packageName, R.layout.clash_notification)
            notification = createNotification("Empty")

            update()
        }
    }

    fun cancel() {
        handler.post {
            remoteViews = null
            notification = null

            NotificationManagerCompat.from(context)
                .cancel(CLASH_STATUS_NOTIFICATION_ID)
        }
    }

    fun setProfile(profile: String) {
        handler.post {
            if ( notification == null )
                return@post

            remoteViews?.setTextViewText(R.id.clash_notification_title, profile)

            notification = createNotification(profile)

            update()
        }
    }

    fun setSpeed(up: Long, down: Long) {
        handler.post {
            if ( notification == null )
                return@post

            remoteViews?.setTextViewText(R.id.clash_notification_up,
                ByteFormatter.byteToStringSecond(up))
            remoteViews?.setTextViewText(R.id.clash_notification_down,
                ByteFormatter.byteToStringSecond(down))

            update()
        }
    }

    private fun update() {
        NotificationManagerCompat.from(context)
            .notify(CLASH_STATUS_NOTIFICATION_ID, notification!!)
    }

    private fun createNotification(profile: String, vpn: Boolean = false): Notification {
        return NotificationCompat.Builder(context, CLASH_STATUS_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setOngoing(true)
            .setColor(context.getColor(R.color.colorAccentService))
            .setColorized(true)
            .setShowWhen(false)
            .setContentTitle(profile)
            .setCustomContentView(remoteViews)
            .setVpn(vpn)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .build()
    }

    private fun NotificationCompat.Builder.setVpn(vpn: Boolean): NotificationCompat.Builder {
        if ( vpn )
            setSubText(context.getString(R.string.clash_service_vpn_mode))
        return this
    }
}