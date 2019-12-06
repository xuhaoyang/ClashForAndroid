package com.github.kr328.clash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.view.View
import com.github.kr328.clash.core.event.*
import com.github.kr328.clash.core.model.GeneralPacket
import com.github.kr328.clash.core.utils.ByteFormatter
import com.github.kr328.clash.service.ClashService
import com.github.kr328.clash.service.TunService
import com.github.kr328.clash.utils.ServiceUtils
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

        activity_main_clash_proxies.setOnClickListener {
            startActivity(Intent(this, ProxyActivity::class.java))
        }

        activity_main_clash_profiles.setOnClickListener {
            startActivity(Intent(this, ProfilesActivity::class.java))
        }

        activity_main_clash_settings.setOnClickListener {
            startActivity(Intent(this, SettingMainActivity::class.java))
        }

        activity_main_clash_logs.setOnClickListener {
            startActivity(Intent(this, LogActivity::class.java))
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
                        ServiceUtils.startProxyService(this)?.also {
                            startActivityForResult(it, VPN_REQUEST_CODE)
                        }
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
                    activity_main_clash_status.setCardBackgroundColor(getColor(R.color.gray))
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

    override fun onProfileReloaded(event: ProfileReloadEvent?) {
        runClash {
            val general = it.queryGeneral()

            runOnUiThread {
                when ( general.mode ) {
                    GeneralPacket.Mode.DIRECT ->
                        activity_main_clash_proxies_summary.text =
                            getText(R.string.clash_proxy_manage_summary_direct)
                    GeneralPacket.Mode.GLOBAL ->
                        activity_main_clash_proxies_summary.text =
                            getText(R.string.clash_proxy_manage_summary_global)
                    GeneralPacket.Mode.RULE ->
                        activity_main_clash_proxies_summary.text =
                            getText(R.string.clash_proxy_manage_summary_rule)
                }
            }
        }
    }

    override fun onBandwidthEvent(event: BandwidthEvent?) {
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
                intArrayOf(Event.EVENT_BANDWIDTH)
            )
        }

        onProfileReloaded(ProfileReloadEvent())
        onProfileChanged(ProfileChangedEvent())
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
                    activity_main_clash_profiles_summary.text =
                        getString(R.string.clash_profiles_summary_selected, profile.name)
                } else {
                    activity_main_clash_profiles_summary.text =
                        getString(R.string.clash_profiles_summary_unselected)
                }
            }
        }
    }
}
