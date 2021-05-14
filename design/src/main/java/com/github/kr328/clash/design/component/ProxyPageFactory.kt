package com.github.kr328.clash.design.component

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kr328.clash.design.view.VerticalScrollableHost

class ProxyPageFactory(private val config: ProxyViewConfig) {
    class Holder(
        val recyclerView: RecyclerView,
        val root: View,
    ) : RecyclerView.ViewHolder(root)

    private val childrenPool = RecyclerView.RecycledViewPool()

    fun newInstance(): Holder {
        val root = VerticalScrollableHost(config.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val recyclerView = RecyclerView(config.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        root.addView(recyclerView)

        recyclerView.apply {
            layoutManager = GridLayoutManager(config.context, 2).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (config.singleLine) 2 else 1
                    }
                }
            }

            setRecycledViewPool(childrenPool)

            clipToPadding = false
        }

        return Holder(recyclerView, root).apply {
            root.tag = this
        }
    }

    fun fromRoot(root: View): Holder {
        return root.tag!! as Holder
    }
}