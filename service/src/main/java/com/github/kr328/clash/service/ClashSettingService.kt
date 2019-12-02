package com.github.kr328.clash.service

import android.content.Context
import androidx.core.content.edit

class ClashSettingService(context: Context): IClashSettingService.Stub() {
    companion object {
        const val KEY_ACCESS_CONTROL_MODE = "key_access_control_mode"
        const val KEY_ACCESS_CONTROL_APPS = "ley_access_control_apps"
        const val KEY_IPV6_ENABLED = "key_ipv6_enabled"
        const val KEY_BYPASS_PRIVATE_NETWORK = "key_bypass_private_network"

        const val ACCESS_CONTROL_MODE_ALLOW_ALL = 0
        const val ACCESS_CONTROL_MODE_ALLOW = 1
        const val ACCESS_CONTROL_MODE_DISALLOW = 2
    }

    private val preference by lazy {
        context.getSharedPreferences("clash_service", Context.MODE_PRIVATE)
    }

    override fun setAccessControl(mode: Int, applications: Array<out String>?) {
        require(mode in 0..3)

        preference.edit {
            putInt(KEY_ACCESS_CONTROL_MODE, mode)
            putStringSet(KEY_ACCESS_CONTROL_APPS, applications?.toSet() ?: emptySet())
        }
    }

    override fun setIPv6Enabled(enabled: Boolean) {
        preference.edit {
            putBoolean(KEY_IPV6_ENABLED, enabled)
        }
    }

    override fun setBypassPrivateNetwork(enabled: Boolean) {
        preference.edit {
            putBoolean(KEY_BYPASS_PRIVATE_NETWORK, enabled)
        }
    }

    override fun isBypassPrivateNetwork(): Boolean {
        return preference.getBoolean(KEY_BYPASS_PRIVATE_NETWORK, true)
    }

    override fun isIPv6Enabled(): Boolean {
        return preference.getBoolean(KEY_IPV6_ENABLED, true)
    }

    override fun getAccessControlApps(): Array<String> {
        return preference.getStringSet(KEY_ACCESS_CONTROL_APPS, emptySet())?.toTypedArray()!!
    }

    override fun getAccessControlMode(): Int {
        return preference.getInt(KEY_ACCESS_CONTROL_MODE, ACCESS_CONTROL_MODE_ALLOW_ALL)
    }
}