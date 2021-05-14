package com.github.kr328.clash

import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.setUUID
import com.github.kr328.clash.common.util.ticker
import com.github.kr328.clash.design.ProfilesDesign
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.util.withProfile
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import java.util.concurrent.TimeUnit

class ProfilesActivity : BaseActivity<ProfilesDesign>() {
    override suspend fun main() {
        val design = ProfilesDesign(this)

        setContentDesign(design)

        val ticker = ticker(TimeUnit.MINUTES.toMillis(1))

        while (isActive) {
            select<Unit> {
                events.onReceive {
                    when (it) {
                        Event.ActivityStart, Event.ProfileChanged -> {
                            design.fetch()
                        }
                        else -> Unit
                    }
                }
                design.requests.onReceive {
                    when (it) {
                        ProfilesDesign.Request.Create ->
                            startActivity(NewProfileActivity::class.intent)
                        ProfilesDesign.Request.UpdateAll ->
                            withProfile {
                                queryAll().forEach { p ->
                                    if (p.imported && p.type != Profile.Type.File)
                                        update(p.uuid)
                                }
                            }
                        is ProfilesDesign.Request.Update ->
                            withProfile { update(it.profile.uuid) }
                        is ProfilesDesign.Request.Delete ->
                            withProfile { delete(it.profile.uuid) }
                        is ProfilesDesign.Request.Edit ->
                            startActivity(PropertiesActivity::class.intent.setUUID(it.profile.uuid))
                        is ProfilesDesign.Request.Active -> {
                            withProfile {
                                if (it.profile.imported)
                                    setActive(it.profile)
                                else
                                    design.requestSave(it.profile)
                            }
                        }
                        is ProfilesDesign.Request.Duplicate -> {
                            val uuid = withProfile { clone(it.profile.uuid) }

                            startActivity(PropertiesActivity::class.intent.setUUID(uuid))
                        }
                    }
                }
                if (activityStarted) {
                    ticker.onReceive {
                        design.updateElapsed()
                    }
                }
            }
        }
    }

    private suspend fun ProfilesDesign.fetch() {
        withProfile {
            patchProfiles(queryAll())
        }
    }
}