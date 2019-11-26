package com.github.kr328.clash

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.VpnService
import android.os.Bundle
import android.view.View
import com.github.kr328.clash.core.event.Event
import com.github.kr328.clash.core.event.ProcessEvent
import com.github.kr328.clash.core.event.ProfileChangedEvent
import com.github.kr328.clash.core.event.TrafficEvent
import com.github.kr328.clash.core.utils.ByteFormatter
import com.github.kr328.clash.service.TunService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_clash_status.*
import kotlinx.android.synthetic.main.activity_main_profiles.*
import kotlinx.android.synthetic.main.activity_main_proxy_manage.*

class MainActivity : BaseActivity() {
    companion object {
        const val VPN_REQUEST_CODE = 233
    }

    private var lastEvent: ProcessEvent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(activity_main_toolbar.apply {
            setLogo(R.mipmap.ic_launcher_foreground)
        })

        activity_main_clash_profiles.setOnClickListener {
            startActivity(Intent(this, ProfilesActivity::class.java))
        }

        activity_main_clash_status_icon.setImageResource(R.drawable.ic_clash_stopped)
        activity_main_clash_status_title.text = getString(R.string.clash_status_stopped)
        activity_main_clash_status_summary.text = getString(R.string.clash_status_click_to_start)
        activity_main_clash_proxies.visibility = View.GONE
        activity_main_clash_logs.visibility = View.GONE

        activity_main_clash_status.setOnClickListener {
            runClash {
                when (it.currentProcessStatus) {
                    ProcessEvent.STARTED -> {
                        it.stop()
                    }
                    else -> runOnUiThread {
                        VpnService.prepare(this@MainActivity)?.apply {
                            startActivityForResult(this, VPN_REQUEST_CODE)
                        } ?: startService(Intent(this@MainActivity, TunService::class.java))
                    }
                }
            }
        }
    }

    override fun onProcessEvent(event: ProcessEvent?) {
        runOnUiThread {
            if (event == lastEvent)
                return@runOnUiThread

            lastEvent = event

            when (event) {
                ProcessEvent.STARTED -> {
                    activity_main_clash_status.setCardBackgroundColor(getColor(R.color.colorAccent))
                    activity_main_clash_status_icon.setImageResource(R.drawable.ic_clash_started)
                    activity_main_clash_status_title.text = getString(R.string.clash_status_started)
                    activity_main_clash_status_summary.text =
                        getString(R.string.clash_status_forwarded_traffic, "0 Bytes")
                    activity_main_clash_proxies.visibility = View.VISIBLE
                    activity_main_clash_logs.visibility = View.VISIBLE
                }
                else -> {
                    activity_main_clash_status.setCardBackgroundColor(Color.GRAY)
                    activity_main_clash_status_icon.setImageResource(R.drawable.ic_clash_stopped)
                    activity_main_clash_status_title.text = getString(R.string.clash_status_stopped)
                    activity_main_clash_status_summary.text =
                        getString(R.string.clash_status_click_to_start)
                    activity_main_clash_proxies.visibility = View.GONE
                    activity_main_clash_logs.visibility = View.GONE
                }
            }
        }
    }

    override fun onTrafficEvent(event: TrafficEvent?) {
        runOnUiThread {
            if (lastEvent == ProcessEvent.STARTED) {
                activity_main_clash_status_summary.text =
                    getString(
                        R.string.clash_status_forwarded_traffic,
                        ByteFormatter.byteToString(event?.total ?: 0)
                    )
            }
        }
    }

    override fun onProfileChanged(event: ProfileChangedEvent?) {
        loadActiveProfile()
    }

    override fun onStart() {
        super.onStart()

        runClash {
            it.eventService.registerEventObserver(
                MainActivity::class.java.simpleName,
                this,
                intArrayOf(Event.EVENT_TRAFFIC)
            )
        }

        loadActiveProfile()
    }

    override fun onStop() {
        super.onStop()

        runClash {
            it.eventService.unregisterEventObserver(MainActivity::class.java.simpleName)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            VPN_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK)
                    startService(Intent(this, TunService::class.java))
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun loadActiveProfile() {
        runClash {
            val profile = it.profileService.queryActiveProfile()

            runOnUiThread {
                if (profile != null) {
                    activity_main_clash_proxies_summary.text =
                        getString(R.string.clash_proxy_manage_summary, profile.proxies)
                    activity_main_clash_profiles_summary.text =
                        getString(R.string.clash_profiles_summary_selected, profile.name)
                } else {
                    activity_main_clash_proxies_summary.text =
                        getString(R.string.clash_proxy_manage_summary, 0)
                    activity_main_clash_profiles_summary.text =
                        getString(R.string.clash_profiles_summary_unselected)
                }
            }
        }
    }
}
