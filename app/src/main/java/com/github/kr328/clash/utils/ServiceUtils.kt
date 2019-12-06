package com.github.kr328.clash.utils

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import com.github.kr328.clash.MainApplication
import com.github.kr328.clash.service.ClashService
import com.github.kr328.clash.service.TunService

object ServiceUtils {
    fun startProxyService(context: Context): Intent? {
        context.getSharedPreferences("application", Context.MODE_PRIVATE).apply {
            when (getString(
                MainApplication.KEY_PROXY_MODE,
                MainApplication.PROXY_MODE_VPN
            )) {
                MainApplication.PROXY_MODE_VPN -> {
                    val prepare = VpnService.prepare(context)

                    if ( prepare != null )
                        return prepare

                    context.startService(Intent(context, TunService::class.java))
                }
                MainApplication.PROXY_MODE_PROXY_ONLY -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(Intent(context, ClashService::class.java))
                    } else {
                        context.startService(Intent(context, ClashService::class.java))
                    }
                }
            }

            return null
        }
    }
}