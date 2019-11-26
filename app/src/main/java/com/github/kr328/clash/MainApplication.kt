package com.github.kr328.clash

import android.app.Application
import com.github.kr328.clash.core.utils.Log
import com.google.firebase.FirebaseApp

class MainApplication : Application() {
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