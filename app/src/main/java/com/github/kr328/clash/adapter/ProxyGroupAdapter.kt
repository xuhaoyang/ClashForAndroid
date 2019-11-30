package com.github.kr328.clash.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kr328.clash.R
import com.github.kr328.clash.core.model.ProxyPacket
import com.github.kr328.clash.model.ListProxyGroup

class ProxyGroupAdapter(private val context: Context) :
    RecyclerView.Adapter<ProxyGroupAdapter.Holder>() {
    var data: List<ListProxyGroup> = emptyList()

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.adapter_proxy_group_name)
        val list: RecyclerView = view.findViewById(R.id.adapter_proxy_group_list)
        val test: View = view.findViewById(R.id.adapter_proxy_group_url_test)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(context)
                .inflate(R.layout.adapter_proxy_group, parent, false)
        ).apply {
            list.layoutManager = object : GridLayoutManager(context, 2) {
                override fun canScrollHorizontally(): Boolean = false
                override fun canScrollVertically(): Boolean = false
            }
            list.adapter = ProxyAdapter(context)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val current = data[position]

        holder.test.visibility =
            if (current.type == ProxyPacket.Type.SELECT) View.VISIBLE else View.GONE
        holder.name.text = current.name

        (holder.list.adapter as ProxyAdapter).apply {
            proxies = current.proxies
            now = current.now
            clickable = current.type == ProxyPacket.Type.SELECT

            notifyDataSetChanged()
        }
    }
}