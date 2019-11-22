package com.github.kr328.clash.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.kr328.clash.R
import com.github.kr328.clash.service.data.ClashProfileEntity

class ProfileAdapter(private val context: Context) :
    RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {
    var profiles: List<ClashProfileEntity> = emptyList()

    class ProfileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkable: View = view.findViewById(R.id.adapter_profiles_checked)
        val title: TextView = view.findViewById(R.id.adapter_profiles_title)
        val summary: TextView = view.findViewById(R.id.adapter_profiles_summary)
        val operation: ImageView = view.findViewById(R.id.adapter_profiles_operation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        return ProfileViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.adapter_profile, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return profiles.size
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val current = profiles[position]

        holder.title.text = current.name
        holder.summary.text = context.getString(
            R.string.clash_profile_item_summary,
            current.proxies, current.proxyGroups, current.rules
        )

        with(current.token) {
            when {
                startsWith("http") || startsWith("content") -> {
                    holder.operation.setImageResource(R.drawable.ic_profile_refresh)
                }
                startsWith("file") -> {
                    holder.operation.setImageResource(R.drawable.ic_profile_edit)
                }
            }
        }
    }
}