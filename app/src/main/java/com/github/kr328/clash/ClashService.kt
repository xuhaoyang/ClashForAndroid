package com.github.kr328.clash

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.ClashProcess
import kotlin.concurrent.thread

class ClashService : Service() {
    companion object {
        private val TAG = "ClashForAndroid"
    }

    private lateinit var clash: Clash
    private inner class ClashServiceImpl : IClashService.Stub() {
        override fun loadProfile(path: String?) {

        }

        override fun startTunDevice(fd: Int, mtu: Int) {

        }
    }

    override fun onCreate() {
        super.onCreate()

        clash = Clash(this,
            filesDir.resolve("clash"),
            cacheDir.resolve("clash_controller"))


        thread {
            clash.exec()

            Log.i(TAG, "Clash exited")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return ClashServiceImpl()
    }

    override fun onDestroy() {


        super.onDestroy()
    }
}