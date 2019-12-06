package com.github.kr328.clash

import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.activity_setting_main.*

class SettingMainActivity : BaseActivity() {
    @Keep
    class Fragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.setting_main, rootKey)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_main)

        setSupportActionBar(activity_setting_main_toolbar)
    }
}