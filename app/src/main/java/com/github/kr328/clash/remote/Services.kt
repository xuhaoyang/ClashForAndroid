package com.github.kr328.clash.remote

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.service.ClashManager
import com.github.kr328.clash.service.ProfileService
import com.github.kr328.clash.service.remote.IClashManager
import com.github.kr328.clash.service.remote.IProfileManager
import com.github.kr328.clash.service.remote.unwrap
import com.github.kr328.clash.util.unbindServiceSilent
import java.util.concurrent.TimeUnit

class Services(private val context: Application, val crashed: () -> Unit) {
    val clash = Resource<IClashManager>()
    val profile = Resource<IProfileManager>()

    private val clashConnection = object : ServiceConnection {
        private var lastCrashed: Long = -1

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            clash.set(service?.unwrap(IClashManager::class))
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            clash.set(null)

            if (System.currentTimeMillis() - lastCrashed < TOGGLE_CRASHED_INTERVAL) {
                unbind()

                crashed()
            }

            lastCrashed = System.currentTimeMillis()

            Log.w("ClashManager crashed")
        }
    }

    private val profileConnection = object : ServiceConnection {
        private var lastCrashed: Long = -1

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            profile.set(service?.unwrap(IProfileManager::class))
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            profile.set(null)

            if (System.currentTimeMillis() - lastCrashed < TOGGLE_CRASHED_INTERVAL) {
                unbind()

                crashed()
            }

            lastCrashed = System.currentTimeMillis()

            Log.w("ProfileService crashed")
        }
    }

    fun bind() {
        try {
            context.bindService(ClashManager::class.intent, clashConnection, Context.BIND_AUTO_CREATE)
            context.bindService(ProfileService::class.intent, profileConnection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            unbind()

            crashed()
        }
    }

    fun unbind() {
        context.unbindServiceSilent(clashConnection)
        context.unbindServiceSilent(profileConnection)

        clash.set(null)
        profile.set(null)
    }

    companion object {
        private val TOGGLE_CRASHED_INTERVAL = TimeUnit.SECONDS.toMillis(10)
    }
}