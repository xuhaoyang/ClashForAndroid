package com.github.kr328.clash.service

import android.content.Context
import com.github.kr328.clash.service.data.ClashDatabase
import com.github.kr328.clash.service.data.ClashProfileEntity
import com.github.kr328.clash.service.data.ClashProfileProxyEntity

class ClashProfileService(context: Context, private val master: Master) :
    IClashProfileService.Stub() {
    interface Master {
        fun preformProfileChanged()
    }

    private val profileDao by lazy {
        ClashDatabase.getInstance(context).openClashProfileDao()
    }
    private val profileProxyDao by lazy {
        ClashDatabase.getInstance(context).openClashProfileProxyDao()
    }

    override fun removeProfile(id: Int) {
        profileDao.removeProfile(id)

        master.preformProfileChanged()
    }

    override fun addProfile(profile: ClashProfileEntity?) {
        require(profile != null)

        profileDao.addProfile(profile)

        master.preformProfileChanged()
    }

    override fun queryActiveProfile(): ClashProfileEntity? {
        return profileDao.queryActiveProfile()
    }

    override fun setActiveProfile(id: Int) {
        profileDao.setActiveProfile(id)

        master.preformProfileChanged()
    }

    override fun queryProfiles(): Array<ClashProfileEntity> {
        return profileDao.queryProfiles()
    }

    fun queryProfileSelected(id: Int): Map<String, String> {
        return profileProxyDao.querySelectedForProfile(id).map {
            it.proxy to it.selected
        }.toMap()
    }

    fun setCurrentProfileProxy(proxy: String, selected: String) {
        val active = profileDao.queryActiveProfile() ?: return

        profileProxyDao.setSelectedForProfile(ClashProfileProxyEntity(active.id, proxy, selected))
    }

    fun removeCurrentProfileProxy(proxies: List<String>) {
        val active = profileDao.queryActiveProfile() ?: return

        profileProxyDao.removeSelectedForProfile(active.id, proxies)
    }
}