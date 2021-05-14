package com.github.kr328.clash

import android.os.DeadObjectException
import com.github.kr328.clash.common.compat.versionCodeCompat
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.design.AppCrashedDesign
import com.github.kr328.clash.log.SystemLogcat
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class AppCrashedActivity : BaseActivity<AppCrashedDesign>() {
    override suspend fun main() {
        val design = AppCrashedDesign(this)

        setContentDesign(design)

        val packageInfo = withContext(Dispatchers.IO) {
            packageManager.getPackageInfo(packageName, 0)
        }

        Log.i("App version: versionName = ${packageInfo.versionName} versionCode = ${packageInfo.versionCodeCompat}")

        val logs = withContext(Dispatchers.IO) {
            SystemLogcat.dumpCrash()
        }

        if (BuildConfig.APP_CENTER_KEY != null && !BuildConfig.DEBUG) {
            if (logs.isNotBlank()) {
                Crashes.trackError(
                    DeadObjectException(),
                    mapOf("type" to "app_crashed"),
                    listOf(ErrorAttachmentLog.attachmentWithText(logs, "logcat.txt"))
                )
            }
        }

        design.setAppLogs(logs)

        while (isActive) {
            events.receive()
        }
    }
}