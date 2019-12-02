package com.github.kr328.clash

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kr328.clash.service.ClashSettingService
import kotlinx.android.synthetic.main.activity_setting_access.*
import kotlin.concurrent.thread

class SettingAccessActivity : BaseActivity() {
    private data class AppInfo(val packageName: String, val name: String, val icon: Drawable)

    private class AppListAdapter(val context: Context) :
        RecyclerView.Adapter<AppListAdapter.Holder>() {

        var applications: List<AppInfo> = emptyList()
        var selected: MutableSet<String> = mutableSetOf()

        class Holder(val view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.adapter_access_app_name)
            val packageName: TextView = view.findViewById(R.id.adapter_access_app_package_name)
            val icon: ImageView = view.findViewById(R.id.adapter_access_app_icon)
            val checkbox: CheckBox = view.findViewById(R.id.adapter_access_app_checkbox)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(
                LayoutInflater.from(context)
                    .inflate(R.layout.adapter_access_app, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return applications.size
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val current = applications[position]

            holder.name.text = current.name
            holder.packageName.text = current.packageName
            holder.icon.setImageDrawable(current.icon)
            holder.checkbox.isChecked = current.packageName in selected

            holder.view.setOnClickListener {
                if (holder.checkbox.isChecked)
                    selected.remove(current.packageName)
                else
                    selected.add(current.packageName)

                notifyItemChanged(position)
            }
        }
    }

    private var showList: Boolean = false
    private var listLoaded: Boolean = false
    private var hidden: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_access)

        setSupportActionBar(activity_setting_access_toolbar)

        activity_setting_access_allow_all.setOnClickListener {
            activity_setting_access_allow_all.isChecked = true
            activity_setting_access_allow.isChecked = false
            activity_setting_access_disallow.isChecked = false

            showList = false

            updateListStatus()
        }

        activity_setting_access_allow.setOnClickListener {
            activity_setting_access_allow_all.isChecked = false
            activity_setting_access_allow.isChecked = true
            activity_setting_access_disallow.isChecked = false

            showList = true

            updateListStatus()
        }

        activity_setting_access_disallow.setOnClickListener {
            activity_setting_access_allow_all.isChecked = false
            activity_setting_access_allow.isChecked = false
            activity_setting_access_disallow.isChecked = true

            showList = true

            updateListStatus()
        }

        activity_setting_access_app_list.apply {
            adapter = AppListAdapter(this@SettingAccessActivity)
            layoutManager = LinearLayoutManager(this@SettingAccessActivity)
            isNestedScrollingEnabled = false
        }

        runClash {
            val settings = it.settingService

            runOnUiThread {
                when (settings.accessControlMode) {
                    ClashSettingService.ACCESS_CONTROL_MODE_ALLOW_ALL ->
                        activity_setting_access_allow_all.performClick()
                    ClashSettingService.ACCESS_CONTROL_MODE_ALLOW ->
                        activity_setting_access_allow.performClick()
                    ClashSettingService.ACCESS_CONTROL_MODE_DISALLOW ->
                        activity_setting_access_disallow.performClick()
                }
            }

            val exclude = resources.getStringArray(R.array.default_disallow_application).toSet()

            val applications = packageManager.getInstalledApplications(0)
                .filterNot {
                    exclude.contains(it.packageName)
                }
                .map { app ->
                    AppInfo(
                        app.packageName,
                        app.loadLabel(packageManager).toString(),
                        app.loadIcon(packageManager)
                    )
                }
                .sortedBy { app ->
                    app.name
                }
            val selected = settings.accessControlApps.toMutableSet()

            runOnUiThread {
                activity_setting_access_app_list.apply {
                    (adapter as AppListAdapter).apply {
                        this.applications = applications
                        this.selected = selected

                        notifyDataSetChanged()
                    }
                }

                listLoaded = true

                updateListStatus()
            }
        }
    }

    override fun onStop() {
        super.onStop()

        runClash {
            val mode = when {
                activity_setting_access_allow_all.isChecked ->
                    ClashSettingService.ACCESS_CONTROL_MODE_ALLOW_ALL
                activity_setting_access_allow.isChecked ->
                    ClashSettingService.ACCESS_CONTROL_MODE_ALLOW
                activity_setting_access_disallow.isChecked ->
                    ClashSettingService.ACCESS_CONTROL_MODE_DISALLOW
                else -> return@runClash
            }

            if (listLoaded)
                it.settingService.setAccessControl(
                    mode,
                    (activity_setting_access_app_list.adapter as AppListAdapter)
                        .selected.toTypedArray()
                )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_setting_access, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_setting_access_hide -> {
                if (hidden) {
                    activity_setting_access_allow_all.visibility = View.VISIBLE
                    activity_setting_access_allow.visibility = View.VISIBLE
                    activity_setting_access_disallow.visibility = View.VISIBLE

                    item.title = getString(R.string.access_setting_hide_header)

                    hidden = false
                } else {
                    if (!activity_setting_access_allow_all.isChecked)
                        activity_setting_access_allow_all.visibility = View.GONE
                    if (!activity_setting_access_allow.isChecked)
                        activity_setting_access_allow.visibility = View.GONE
                    if (!activity_setting_access_disallow.isChecked)
                        activity_setting_access_disallow.visibility = View.GONE

                    item.title = getString(R.string.access_setting_show_header)

                    hidden = true
                }
            }
            R.id.menu_setting_access_select_all -> {
                thread {
                    val selected = (activity_setting_access_app_list.adapter as AppListAdapter)
                        .applications.map { it.packageName }.toMutableSet()

                    runOnUiThread {
                        (activity_setting_access_app_list.adapter as AppListAdapter).apply {
                            this.selected = selected

                            notifyItemRangeChanged(0, applications.size)
                        }
                    }
                }
            }
            R.id.menu_setting_access_select_invert -> {
                thread {
                    (activity_setting_access_app_list.adapter as AppListAdapter).apply {
                        selected =
                            (applications.map { it.packageName }.toSet() - selected).toMutableSet()
                    }

                    runOnUiThread {
                        (activity_setting_access_app_list.adapter as AppListAdapter).apply {
                            notifyItemRangeChanged(0, applications.size)
                        }
                    }
                }
            }
            R.id.menu_setting_access_clean_selection -> {
                (activity_setting_access_app_list.adapter as AppListAdapter).apply {
                    selected = mutableSetOf()

                    notifyItemRangeChanged(0, applications.size)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    private fun updateListStatus() {
        if (showList)
            activity_setting_access_divider.visibility = View.VISIBLE
        else
            activity_setting_access_divider.visibility = View.GONE


        if (showList) {
            if (listLoaded)
                activity_setting_access_loading.visibility = View.GONE
            else
                activity_setting_access_loading.visibility = View.VISIBLE
        } else
            activity_setting_access_loading.visibility = View.GONE


        if (showList && listLoaded)
            activity_setting_access_app_list.visibility = View.VISIBLE
        else
            activity_setting_access_app_list.visibility = View.GONE
    }
}