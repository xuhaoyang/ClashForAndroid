package com.github.kr328.clash.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.kr328.clash.R
import com.github.kr328.clash.core.model.Proxy

class ProxyGroupAdapter(private val context: Context) : RecyclerView.Adapter<ProxyGroupAdapter.Holder>() {
    var data: List<Pair<String, Proxy>> = emptyList()

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.adapter_proxy_group_name)
        val list = view.findViewById<RecyclerView>(R.id.adapter_proxy_group_list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context)
            .inflate(R.layout.adapter_proxy_group, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val current = data[position]

        holder.name.text = current.first
    }
}