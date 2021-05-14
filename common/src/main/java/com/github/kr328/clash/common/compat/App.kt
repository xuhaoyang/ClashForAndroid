package com.github.kr328.clash.common.compat

import android.app.ActivityThread
import android.app.Application
import android.os.Build
import com.github.kr328.clash.common.log.Log

val Application.currentProcessName: String
    get() {
        if (Build.VERSION.SDK_INT >= 28)
            return Application.getProcessName()

        return try {
            ActivityThread.currentProcessName()
        } catch (throwable: Throwable) {
            Log.w("Resolve process name: $throwable")

            packageName
        }
    }
