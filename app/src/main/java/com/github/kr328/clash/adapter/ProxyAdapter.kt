package com.github.kr328.clash.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kr328.clash.R
import com.github.kr328.clash.core.model.ProxyPacket
import com.github.kr328.clash.model.ListProxy
import com.google.android.material.card.MaterialCardView

class ProxyAdapter(private val context: Context,
                   private val onSelect: (String, String) -> Unit,
                   private val onUrlTest: (Int, Int) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var elements: List<ListProxy> = emptyList()
    var clickable: Boolean = false

    class HeaderHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.adapter_proxy_header_name)
        val test: View = view.findViewById(R.id.adapter_proxy_header_url_test)
    }

    class ItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.adapter_proxy_item_name)
        val type: TextView = view.findViewById(R.id.adapter_proxy_item_type)
        val delay: TextView = view.findViewById(R.id.adapter_proxy_item_delay)
        val card: MaterialCardView = view.findViewById(R.id.adapter_proxy_item_card)
    }

    override fun getItemViewType(position: Int): Int {
        return when (elements[position]) {
            is ListProxy.ListProxyItem -> 1
            is ListProxy.ListProxyHeader -> 2
            else -> throw IllegalArgumentException("Invalid type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> ItemHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.adapter_proxy_item,
                    parent,
                    false
                )
            )
            2 -> HeaderHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.adapter_proxy_header,
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Invalid type")
        }
    }

    override fun getItemCount(): Int {
        return elements.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val current = elements[position]) {
            is ListProxy.ListProxyItem -> bindItemView(holder as ItemHolder, current, position)
            is ListProxy.ListProxyHeader -> bindHeaderView(holder as HeaderHolder, current)
        }
    }

    fun getLayoutManager(): GridLayoutManager {
        return GridLayoutManager(context, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (elements[position]) {
                        is ListProxy.ListProxyHeader -> 2
                        is ListProxy.ListProxyItem -> 1
                        else -> throw IllegalArgumentException("Invalid type")
                    }
                }
            }
        }
    }

    private fun bindItemView(holder: ItemHolder, current: ListProxy.ListProxyItem, position: Int) {
        if (position == current.header.now) {
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

        if (current.header.type == ProxyPacket.Type.SELECT) {
            holder.card.isFocusable = true
            holder.card.isClickable = true
            holder.card.setOnClickListener {
                val element = (elements[position] as ListProxy.ListProxyItem)

                val old = element.header.now
                element.header.now = position

                notifyItemChanged(old)
                notifyItemChanged(position)

                onSelect(element.header.name, element.name)
            }
        } else {
            holder.card.setOnClickListener(null)
            holder.card.isFocusable = false
            holder.card.isClickable = false
        }

        holder.name.text = current.name
        holder.type.text = current.type
        holder.delay.text =
            when {
                current.header.urlTest && current.delay < 0 -> "..."
                current.delay < 0 -> "N/A"
                current.delay > 0 -> {
                    current.delay.toString()
                }
                else -> {
                    if (current.header.type != ProxyPacket.Type.SELECT)
                        "N/A"
                    else
                        ""
                }
            }
    }

    private fun bindHeaderView(holder: HeaderHolder, current: ListProxy.ListProxyHeader) {
        holder.name.text = current.name
        holder.test.visibility =
            if (current.type == ProxyPacket.Type.SELECT) View.VISIBLE else View.GONE
        holder.test.setOnClickListener {
            if ( current.urlTest )
                return@setOnClickListener

            val indexed = elements.withIndex()
                .filter { it.value is ListProxy.ListProxyHeader }
                .filterIsInstance<IndexedValue<ListProxy.ListProxyHeader>>()

            val headerIndex = indexed.indexOfFirst {
                it.value === current
            }.takeIf { it >= 0 } ?: return@setOnClickListener
            val header = indexed[headerIndex]

            indexed[headerIndex].value.urlTest = true

            val position = header.index
            val size = (indexed.getOrNull(headerIndex + 1)?.index ?: elements.size) - header.index

            notifyItemRangeChanged(position, size)
            onUrlTest(position, size)
        }
    }
}