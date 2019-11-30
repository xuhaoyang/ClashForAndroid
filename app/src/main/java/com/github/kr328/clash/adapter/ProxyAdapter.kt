package com.github.kr328.clash.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.kr328.clash.R
import com.github.kr328.clash.model.ListProxyGroup
import com.google.android.material.card.MaterialCardView

class ProxyAdapter(private val context: Context) : RecyclerView.Adapter<ProxyAdapter.Holder>() {
    var proxies: List<ListProxyGroup.ListProxy> = emptyList()
    var now: ListProxyGroup.ListProxy? = null
    var clickable: Boolean = false

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.adapter_proxy_name)
        val type: TextView = view.findViewById(R.id.adapter_proxy_type)
        val delay: TextView = view.findViewById(R.id.adapter_proxy_delay)
        val card: MaterialCardView = view.findViewById(R.id.adapter_proxy_card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.adapter_proxy, parent, false))
    }

    override fun getItemCount(): Int {
        return proxies.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val current = proxies[position]

        if (current === now) {
            holder.card.setCardBackgroundColor(context.getColor(R.color.colorAccent))
            holder.name.setTextColor(Color.WHITE)
            holder.type.setTextColor(Color.WHITE - 0x22222222)
            holder.delay.setTextColor(Color.WHITE - 0x11111111)
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE)
            holder.name.setTextColor(Color.BLACK)
            holder.type.setTextColor(Color.LTGRAY)
            holder.delay.setTextColor(Color.DKGRAY)
        }

        holder.card.isFocusable = clickable
        holder.card.isClickable = clickable

        holder.name.text = current.name
        holder.type.text = current.type
        holder.delay.text = if (current.delay > 0) current.delay.toString() else ""

        if (clickable) {
            holder.card.setOnClickListener {
                notifyItemChanged(proxies.indexOf(now))
                notifyItemChanged(position)

                now = current
            }
        }
    }
}