package com.github.kr328.clash

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_setting_access.*

class SettingAccessActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_access)

        setSupportActionBar(activity_setting_access_toolbar)

        activity_setting_access_allow_all.setOnClickListener {
            activity_setting_access_allow_all.isChecked = true
            activity_setting_access_allow.isChecked = false
            activity_setting_access_disallow.isChecked = false

            activity_setting_access_divider.visibility = View.GONE
            activity_setting_access_app_list.visibility = View.GONE
        }

        activity_setting_access_allow.setOnClickListener {
            activity_setting_access_allow_all.isChecked = false
            activity_setting_access_allow.isChecked = true
            activity_setting_access_disallow.isChecked = false

            activity_setting_access_divider.visibility = View.VISIBLE
            activity_setting_access_app_list.visibility = View.VISIBLE
        }

        activity_setting_access_disallow.setOnClickListener {
            activity_setting_access_allow_all.isChecked = false
            activity_setting_access_allow.isChecked = false
            activity_setting_access_disallow.isChecked = true

            activity_setting_access_divider.visibility = View.VISIBLE
            activity_setting_access_app_list.visibility = View.VISIBLE
        }
    }
}