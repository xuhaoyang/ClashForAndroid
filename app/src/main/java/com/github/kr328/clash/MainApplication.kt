package com.github.kr328.clash

import android.app.Application
import android.content.Context
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.compat.currentProcessName
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.remote.Remote
import com.github.kr328.clash.service.util.sendServiceRecreated
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes

@Suppress("unused")
class MainApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        Global.init(this)
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize AppCenter
        if (BuildConfig.APP_CENTER_KEY != null && !BuildConfig.DEBUG) {
            AppCenter.start(
                this,
                BuildConfig.APP_CENTER_KEY,
                Analytics::class.java, Crashes::class.java
            )
        }

        val processName = currentProcessName

        Log.d("Process $processName started")

        if (processName == packageName) {
            Remote.launch()
        } else {
            sendServiceRecreated()
        }
    }
}