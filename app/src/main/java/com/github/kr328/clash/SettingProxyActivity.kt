package com.github.kr328.clash

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.activity_setting_proxy.*

class SettingProxyActivity : BaseActivity() {
    class Fragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.setting_proxy, rootKey)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_proxy)

        setSupportActionBar(activity_setting_proxy_toolbar)

        activity_setting_proxy_vpn_mode.setOnClickListener {
            activity_setting_proxy_vpn_mode.isChecked = true
            activity_setting_proxy_proxy_only_mode.isChecked = false

            activity_setting_proxy_divider.visibility = View.VISIBLE
            activity_setting_proxy_content.visibility = View.VISIBLE
        }

        activity_setting_proxy_proxy_only_mode.setOnClickListener {
            activity_setting_proxy_vpn_mode.isChecked = false
            activity_setting_proxy_proxy_only_mode.isChecked = true

            activity_setting_proxy_divider.visibility = View.GONE
            activity_setting_proxy_content.visibility = View.GONE
        }
    }
}