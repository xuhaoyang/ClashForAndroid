package com.github.kr328.clash.common.compat

import android.app.PendingIntent
import android.os.Build

fun pendingIntentFlags(flags: Int, immutable: Boolean = false): Int {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && immutable) {
        flags or PendingIntent.FLAG_IMMUTABLE
    } else {
        flags
    }
}
