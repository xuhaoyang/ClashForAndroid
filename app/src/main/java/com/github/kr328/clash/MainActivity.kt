package com.github.kr328.clash

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE = 233
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).setType("*/*"), REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ( requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK ) {
            Toast.makeText(this, intent?.dataString, Toast.LENGTH_LONG).show()

            thread {
                FileOutputStream(cacheDir.resolve("config.yaml")).use {
                    contentResolver.openInputStream(data?.data ?: throw RuntimeException())?.apply { copyTo(it) }?.close()
                }

                bindService(Intent(this, ClashService::class.java), object: ServiceConnection {
                    override fun onServiceDisconnected(name: ComponentName?) {
                        Toast.makeText(this@MainActivity, "Disconnected", Toast.LENGTH_LONG).show()
                    }

                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        try {
                            val clash = IClashService.Stub.asInterface(service)

                            clash.loadProfile(cacheDir.resolve("config.yaml").absolutePath)
                        }
                        catch (e: Exception) {
                            Log.e("ClashForAndroid", "Load", e)
                        }
                    }

                }, Context.BIND_AUTO_CREATE)
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
