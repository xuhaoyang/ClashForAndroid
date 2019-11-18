package com.github.kr328.clash

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.github.kr328.clash.core.Clash
import java.lang.Exception
import kotlin.concurrent.thread

class ClashService : Service() {
    private inner class ClashServiceImpl : IClashService.Stub() {
        override fun loadProfile(path: String?) {
            thread {
                Clash.loadProfileFromPath(path)
            }
        }

        override fun startTunDevice(fd: Int, mtu: Int) {
            Clash.startTun(fd, mtu)
        }
    }

    override fun onCreate() {
        super.onCreate()

        Clash.init(
            applicationInfo.nativeLibraryDir + "/libclash.so",
            filesDir.resolve("clash").absolutePath
        )
        Clash.loadDefault()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return ClashServiceImpl()
    }

    override fun onDestroy() {
        Clash.loadDefault()

        super.onDestroy()
    }
}