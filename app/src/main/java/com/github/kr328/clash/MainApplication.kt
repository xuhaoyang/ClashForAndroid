package com.github.kr328.clash

import android.app.Application
import com.github.kr328.clash.core.utils.Log
import com.google.firebase.FirebaseApp

class MainApplication : Application() {
    companion object {
        const val KEY_PROXY_MODE = "key_proxy_mode"
        const val PROXY_MODE_VPN = "vpn"
        const val PROXY_MODE_PROXY_ONLY = "proxy_only"
    }

    override fun onCreate() {
        super.onCreate()

        try {
            FirebaseApp.initializeApp(this)
            Log.i("Registered")
        } catch (e: IllegalStateException) {
            Log.i("Already registered")
        }
    }
}