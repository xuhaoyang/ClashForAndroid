package com.github.kr328.clash

import android.content.ComponentName
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.activity_setting_application.*
import kotlin.concurrent.thread

class SettingApplicationActivity : BaseActivity() {
    companion object {
        const val KEY_START_ON_BOOT = "key_application_start_on_boot"
    }

    class Fragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.setting_application, rootKey)

            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onStart() {
            super.onStart()

            val status = requireContext().packageManager
                .getComponentEnabledSetting(
                    ComponentName.createRelative(
                        requireContext(),
                        BootCompleteReceiver::class.java.name
                    )
                )

            findPreference<CheckBoxPreference>(KEY_START_ON_BOOT)?.isChecked =
                status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            when (key) {
                KEY_START_ON_BOOT -> {
                    val enabled = sharedPreferences!!.getBoolean(key, false)

                    thread {
                        if (enabled) {
                            requireContext().packageManager
                                .setComponentEnabledSetting(
                                    ComponentName.createRelative(
                                        requireContext(),
                                        BootCompleteReceiver::class.java.name
                                    ),
                                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                    PackageManager.DONT_KILL_APP
                                )
                        } else {
                            requireContext().packageManager
                                .setComponentEnabledSetting(
                                    ComponentName.createRelative(
                                        requireContext(),
                                        BootCompleteReceiver::class.java.name
                                    ),
                                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                    PackageManager.DONT_KILL_APP
                                )
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_application)

        setSupportActionBar(activity_setting_application_toolbar)
    }
}