package com.github.kr328.clash

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.github.kr328.clash.adapter.ProfileAdapter
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.event.ProfileChangedEvent
import com.github.kr328.clash.model.ClashProfile
import com.github.kr328.clash.service.data.ClashProfileEntity
import com.github.kr328.clash.utils.FileUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_import_url.*
import kotlinx.android.synthetic.main.activity_profiles.*
import java.io.FileOutputStream
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import kotlin.concurrent.thread

class ProfilesActivity : BaseActivity() {
    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profiles)

        setSupportActionBar(activity_profiles_toolbar)

        activity_profiles_new_profile.setOnClickListener {
            startActivity(Intent(this, CreateProfileActivity::class.java))
        }

        activity_profiles_main_list.layoutManager = object : LinearLayoutManager(this) {
            override fun canScrollHorizontally(): Boolean = false
            override fun canScrollVertically(): Boolean = false
        }
        activity_profiles_main_list.adapter = ProfileAdapter(this,
            this::onProfileClick,
            this::onOperateClick,
            this::onProfileLongClick)
    }

    override fun onStart() {
        super.onStart()

        runClash {
            it.eventService.registerEventObserver(
                ProfilesActivity::class.java.simpleName,
                this,
                intArrayOf()
            )
        }

        reloadList()
    }

    override fun onStop() {
        super.onStop()

        runClash {
            it.eventService.unregisterEventObserver(ProfilesActivity::class.java.simpleName)
        }
    }

    override fun onProfileChanged(event: ProfileChangedEvent?) {
        reloadList()
    }

    private fun reloadList() {
        runClash {
            refreshList(it.profileService.queryProfiles())
        }
    }

    private fun refreshList(newData: Array<ClashProfileEntity>) {
        val adapter = activity_profiles_main_list.adapter as ProfileAdapter
        val oldData = adapter.profiles

        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldData[oldItemPosition].id == newData[newItemPosition].id

            override fun getOldListSize(): Int = oldData.size

            override fun getNewListSize(): Int = newData.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldData[oldItemPosition] == newData[newItemPosition]
        })

        runOnUiThread {
            adapter.profiles = newData
            result.dispatchUpdatesTo(adapter)
        }
    }

    private fun onProfileClick(profile: ClashProfileEntity) {
        runClash {
            it.profileService.setActiveProfile(profile.id)
        }
    }

    private fun onOperateClick(profile: ClashProfileEntity) {
        when {
            ClashProfileEntity.isUrlToken(profile.token) -> {
                dialog?.dismiss()

                dialog = AlertDialog.Builder(this)
                    .setTitle(R.string.clash_profile_updating)
                    .setView(R.layout.dialog_profile_updating)
                    .setCancelable(false)
                    .show()

                    updateProfile(profile)
            }
            ClashProfileEntity.isFileToken(profile.token) -> {
                Snackbar.make(
                    activity_profiles_root,
                    R.string.not_implemented,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun onProfileLongClick(parent: View, profile: ClashProfileEntity) {
        PopupMenu(this, parent).apply {
            setOnMenuItemClickListener { removeProfile(profile).run { true } }
            inflate(R.menu.menu_profile_popup)
            show()
        }
    }

    private fun removeProfile(profile: ClashProfileEntity) {
        runClash {
            it.profileService.removeProfile(profile.id)
        }
    }

    private fun updateProfile(profile: ClashProfileEntity) {
        val url = ClashProfileEntity.getUrl(profile.token)

        runClash {
            val httpPort = it.queryGeneral().ports.randomHttp

            thread {
                try {
                    val connection = if ( httpPort > 0 )
                        URL(url).openConnection(
                            Proxy(
                                Proxy.Type.HTTP,
                                InetSocketAddress.createUnresolved("127.0.0.1", httpPort))
                        )
                    else
                        URL(url).openConnection()

                    val data = with (connection) {
                        connectTimeout = ImportUrlActivity.DEFAULT_TIMEOUT
                        connect()

                        getInputStream().bufferedReader().use {
                            it.readText()
                        }
                    }

                    Yaml(configuration = YamlConfiguration(strictMode = false)).parse(
                        ClashProfile.serializer(), data)

                    FileOutputStream(profile.file).use { outputStream ->
                        outputStream.write(data.toByteArray())
                    }

                    runClash { clash ->
                        clash.profileService.addProfile(profile
                            .copy(lastUpdate = System.currentTimeMillis()))
                    }
                }
                catch (e: Exception) {
                    runOnUiThread {
                        Snackbar.make(
                            activity_profiles_root,
                            getString(R.string.clash_profile_invalid, e.toString()),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }

                runOnUiThread {
                    dialog?.dismiss()
                }
            }
        }
    }
}