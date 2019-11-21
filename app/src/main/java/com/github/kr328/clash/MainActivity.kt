package com.github.kr328.clash

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import com.github.kr328.clash.core.ClashProcessStatus
import com.github.kr328.clash.service.ClashService
import com.github.kr328.clash.service.IClashObserver
import com.github.kr328.clash.service.IClashService
import com.github.kr328.clash.service.TunService
import com.github.kr328.clash.service.data.ClashDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_clash_status.*
import kotlinx.android.synthetic.main.activity_main_profiles.*
import kotlinx.android.synthetic.main.activity_main_proxy_manage.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val VPN_REQUEST_CODE = 233
    }

    private val handler = Handler()
    private var clash: IClashService? = null
    private val connection = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            finish()
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            clash = IClashService.Stub.asInterface(service)

            clash?.apply {
                registerObserver("main_activity",true , object: IClashObserver.Stub() {
                    override fun onStatusChanged(status: ClashProcessStatus?) {
                        clashStatus.postValue(status ?: ClashProcessStatus(ClashProcessStatus.STATUS_STOPPED_INT))
                    }
                })
            }
        }
    }

    private val clashStatus = MutableLiveData<ClashProcessStatus>(ClashProcessStatus(ClashProcessStatus.STATUS_STOPPED_INT))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(activity_main_toolbar.apply {
            setLogo(R.mipmap.ic_launcher_foreground)
        })

        activity_main_clash_status_icon.setImageResource(R.drawable.ic_clash_stopped)
        activity_main_clash_status_title.text = getString(R.string.clash_status_stopped)
        activity_main_clash_status_summary.text = getString(R.string.clash_status_click_to_start)
        activity_main_clash_proxies.visibility = View.GONE
        activity_main_clash_logs.visibility = View.GONE

        clashStatus.observe(this) {
            handler.removeMessages(0)
            handler.postDelayed({
                when ( it ) {
                    ClashProcessStatus.STATUS_STARTED -> {
                        activity_main_clash_status_icon.setImageResource(R.drawable.ic_clash_started)
                        activity_main_clash_status_title.text = getString(R.string.clash_status_started)
                        activity_main_clash_status_summary.text = getString(R.string.clash_status_forwarded_traffic, "1.4GiB")
                        activity_main_clash_proxies.visibility = View.VISIBLE
                        activity_main_clash_logs.visibility = View.VISIBLE
                    }
                    else -> {
                        activity_main_clash_status_icon.setImageResource(R.drawable.ic_clash_stopped)
                        activity_main_clash_status_title.text = getString(R.string.clash_status_stopped)
                        activity_main_clash_status_summary.text = getString(R.string.clash_status_click_to_start)
                        activity_main_clash_proxies.visibility = View.GONE
                        activity_main_clash_logs.visibility = View.GONE
                    }
                }
            }, 300)
        }

        activity_main_clash_status.setOnClickListener {
            when ( clashStatus.value ?: ClashProcessStatus.STATUS_STOPPED ) {
                ClashProcessStatus.STATUS_STARTED -> {}
                ClashProcessStatus.STATUS_STOPPED -> {
                    VpnService.prepare(this)?.apply {
                         startActivityForResult(this, VPN_REQUEST_CODE)
                    } ?: startService(Intent(this, TunService::class.java))
                }
            }
        }

        ClashDatabase.getInstance(this)
            .openClashProfileDao()
            .observeDefaultProfileUrl()
            .observe(this) {
                activity_main_clash_profiles_summary.text =
                    if ( it == null )
                        getString(R.string.clash_profiles_summary_unselected)
                    else
                        getString(R.string.clash_profiles_summary_selected, it)
            }
    }

    override fun onStart() {
        super.onStart()

        bindService(Intent(this, ClashService::class.java),
            connection,
            Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()

        unbindService(connection)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when ( requestCode ) {
            VPN_REQUEST_CODE -> {
                if ( resultCode == Activity.RESULT_OK )
                    startService(Intent(this, TunService::class.java))
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}
