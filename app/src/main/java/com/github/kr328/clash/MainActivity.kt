package com.github.kr328.clash

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE = 233
        private const val VPN_REQUEST = 234
    }

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Toast.makeText(this@MainActivity, "Disconnected", Toast.LENGTH_LONG).show()
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                val clash = IClashService.Stub.asInterface(service)

                clash.loadProfile(cacheDir.resolve("config.yaml").absolutePath)


            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).setType("*/*"), REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, intent?.dataString, Toast.LENGTH_LONG).show()

            thread {
                bindService(Intent(this, ClashService::class.java), object : ServiceConnection {
                    override fun onServiceDisconnected(name: ComponentName?) {
                        Toast.makeText(this@MainActivity, "Disconnected", Toast.LENGTH_LONG).show()
                    }

                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        IClashService.Stub.asInterface(service)?.loadProfile(data?.dataString)

                        VpnService.prepare(this@MainActivity)?.apply {
                            startActivityForResult(this, VPN_REQUEST)
                        } ?: startService(Intent(this@MainActivity, TunService::class.java))
                    }

                }, Context.BIND_AUTO_CREATE)
            }
        } else if (requestCode == VPN_REQUEST && resultCode == Activity.RESULT_OK) {
            startService(Intent(this, TunService::class.java))
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
