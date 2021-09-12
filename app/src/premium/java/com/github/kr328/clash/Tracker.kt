package com.github.kr328.clash

import android.app.Application
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog

object Tracker {
    fun initialize(application: Application) {
        if (!BuildConfig.DEBUG) {
            AppCenter.start(
                application,
                BuildConfig.APP_CENTER_KEY,
                Analytics::class.java, Crashes::class.java
            )
        }
    }

    fun uploadLogcat(logcat: String) {
        if (!BuildConfig.DEBUG) {
            if (logcat.isNotBlank()) {
                Crashes.trackError(
                    RuntimeException(),
                    mapOf("type" to "app_crashed"),
                    listOf(ErrorAttachmentLog.attachmentWithText(logcat, "logcat.txt"))
                )
            }
        }
    }
}