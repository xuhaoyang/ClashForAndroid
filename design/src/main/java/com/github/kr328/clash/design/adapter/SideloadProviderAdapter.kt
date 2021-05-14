package com.github.kr328.clash.design.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.kr328.clash.design.databinding.AdapterSideloadProviderBinding
import com.github.kr328.clash.design.model.AppInfo
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.root

class SideloadProviderAdapter(
    private val context: Context,
    private val apps: List<AppInfo>,
    var selectedPackageName: String
) : RecyclerView.Adapter<SideloadProviderAdapter.Holder>() {
    class Holder(val binding: AdapterSideloadProviderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            AdapterSideloadProviderBinding
                .inflate(context.layoutInflater, context.root, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val current = apps[position]

        holder.binding.appInfo = current

        holder.binding.selected = selectedPackageName == current.packageName

        holder.binding.root.setOnClickListener {
            val index = apps.indexOfFirst { it.packageName == selectedPackageName }

            selectedPackageName = current.packageName

            if (index >= 0)
                notifyItemChanged(index)

            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return apps.size
    }
}