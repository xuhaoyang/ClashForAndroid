package com.github.kr328.clash

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.ClashException
import java.io.File
import kotlin.concurrent.thread

class ClashService : Service() {
    private val binder = object: IClashService.Stub() {
        override fun loadProfile(path: String?) {
            if ( path == null || !File(path).exists() )
                throw RemoteException("Invalid path")

            thread {
                try {
                    Clash.loadProfileFromPath(path)
                }
                catch (e: ClashException) {
                    Log.w("ClashForAndroid", "Load failure", e)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        Clash.init(filesDir.resolve("clash").absolutePath)
        Clash.loadDefault()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        Clash.loadDefault()

        super.onDestroy()
    }
}