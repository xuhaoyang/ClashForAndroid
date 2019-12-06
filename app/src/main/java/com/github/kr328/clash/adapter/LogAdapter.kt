package com.github.kr328.clash.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.collection.CircularArray
import androidx.recyclerview.widget.RecyclerView
import com.github.kr328.clash.R
import com.github.kr328.clash.core.event.LogEvent
import kotlinx.android.synthetic.main.adapter_log.view.*
import java.text.SimpleDateFormat
import java.util.*

class LogAdapter(private val content: Context,
                 private val buffer: CircularArray<LogEvent>) : RecyclerView.Adapter<LogAdapter.Holder>() {
    private val formatter = SimpleDateFormat("hh:MM:ss", Locale.getDefault())

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val type: TextView = view.findViewById(R.id.adapter_log_type)
        val time: TextView = view.findViewById(R.id.adapter_log_time)
        val content: TextView = view.findViewById(R.id.adapter_log_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(content)
            .inflate(R.layout.adapter_log, parent, false))
    }

    override fun getItemCount(): Int {
        return buffer.size()
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val current = buffer.get(position)

        holder.type.text = current.level.toString()
        holder.time.text = formatter.format(Date(current.time))
        holder.content.text = current.message
    }
}