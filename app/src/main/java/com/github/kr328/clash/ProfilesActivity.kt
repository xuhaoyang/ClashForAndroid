package com.github.kr328.clash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kr328.clash.adapter.ProfileAdapter
import com.github.kr328.clash.service.data.ClashDatabase
import com.github.kr328.clash.service.data.ClashProfileEntity
import kotlinx.android.synthetic.main.activity_profiles.*
import kotlin.concurrent.thread

class ProfilesActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profiles)

        setSupportActionBar(activity_profiles_toolbar)
        activity_profiles_toolbar.elevation = 4.0f

        activity_profiles_new_profile.setOnClickListener {
            startActivity(Intent(this, CreateProfileActivity::class.java))
        }

        activity_profiles_main_list.layoutManager = object: LinearLayoutManager(this) {
            override fun canScrollHorizontally(): Boolean = false
            override fun canScrollVertically(): Boolean = false
        }
        activity_profiles_main_list.adapter = ProfileAdapter(this) {
            thread {
                ClashDatabase.getInstance(this)
                    .openClashProfileDao()
                    .setSelectedProfile(it)
            }
        }

        ClashDatabase.getInstance(this)
            .openClashProfileDao()
            .observeProfiles()
            .observe(this, this::refreshList)
    }

    private fun refreshList(newData: List<ClashProfileEntity>) {
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

        adapter.profiles = newData
        result.dispatchUpdatesTo(adapter)
    }
}