package com.github.kr328.clash.service

import android.content.Context
import com.github.kr328.clash.service.data.ClashDatabase
import com.github.kr328.clash.service.data.ClashProfileEntity

class ClashProfileService(context: Context, private val master: Master) :
    IClashProfileService.Stub() {
    interface Master {
        fun preformProfileChanged()
    }

    val database by lazy {
        ClashDatabase.getInstance(context).openClashProfileDao()
    }

    override fun removeProfile(id: Int) {
        database.removeProfile(id)

        master.preformProfileChanged()
    }

    override fun addProfile(profile: ClashProfileEntity?) {
        require(profile != null)

        database.addProfile(profile)

        master.preformProfileChanged()
    }

    override fun queryActiveProfile(): ClashProfileEntity? {
        return database.queryActiveProfile()
    }

    override fun setActiveProfile(id: Int) {
        database.setActiveProfile(id)

        master.preformProfileChanged()
    }

    override fun queryProfiles(): Array<ClashProfileEntity> {
        return database.queryProfiles()
    }
}