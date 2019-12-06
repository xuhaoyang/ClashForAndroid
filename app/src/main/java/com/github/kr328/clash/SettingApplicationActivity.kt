package com.github.kr328.clash

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.github.kr328.clash.core.utils.Log
import kotlinx.android.synthetic.main.activity_setting_application.*
import kotlin.concurrent.thread

class SettingApplicationActivity : BaseActivity() {
    companion object {
        const val KEY_START_ON_BOOT = "key_application_start_on_boot"
    }

    @Keep
    class Fragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.setting_application, rootKey)
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)

            val status = context.packageManager
                .getComponentEnabledSetting(
                    ComponentName.createRelative(
                        requireContext(),
                        BootCompleteReceiver::class.java.name
                    )
                )

            findPreference<CheckBoxPreference>(KEY_START_ON_BOOT)?.also {
                it.isChecked = status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                it.onPreferenceChangeListener = this
            }

        }

        override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
            if ( !isAdded )
                return false

            when (preference?.key) {
                KEY_START_ON_BOOT -> {
                    val enabled = newValue as Boolean? ?: false

                    thread {
                        try {
                            setBootCompleteReceiverEnabled(enabled)
                        }
                        catch (e: Exception) {
                            Log.w("Set boot complete failure", e)
                        }
                    }
                }
            }

            return true
        }

        private fun setBootCompleteReceiverEnabled(enabled: Boolean) {
            if (enabled) {
                requireActivity().packageManager
                    .setComponentEnabledSetting(
                        ComponentName.createRelative(
                            requireActivity(),
                            BootCompleteReceiver::class.java.name
                        ),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
            } else {
                requireActivity().packageManager
                    .setComponentEnabledSetting(
                        ComponentName.createRelative(
                            requireActivity(),
                            BootCompleteReceiver::class.java.name
                        ),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_application)

        setSupportActionBar(activity_setting_application_toolbar)
    }


}