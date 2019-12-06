package com.github.kr328.clash.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.kr328.clash.R
import com.github.kr328.clash.service.data.ClashProfileEntity
import com.github.kr328.clash.view.RadioFatItem
import java.text.SimpleDateFormat
import java.util.*

class ProfileAdapter(private val context: Context,
                     private val onClick: (ClashProfileEntity) -> Unit,
                     private val onOperateClick: (ClashProfileEntity) -> Unit,
                     private val onLongClicked: (View,ClashProfileEntity) -> Unit) :
    RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {
    var profiles: Array<ClashProfileEntity> = emptyArray()

    class ProfileViewHolder(val view: RadioFatItem) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        return ProfileViewHolder(
            RadioFatItem(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        )
    }

    override fun getItemCount(): Int {
        return profiles.size
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val current = profiles[position]

        holder.view.title = current.name
        holder.view.isChecked = current.active
        holder.view.setOnClickListener {
            onClick(current)
        }
        holder.view.setOnOperationOnClickListener(View.OnClickListener {
            onOperateClick(current)
        })
        holder.view.setOnLongClickListener {
            onLongClicked(it, current).run { true }
        }

        val profileUpdateDate = GregorianCalendar().apply {
            timeInMillis = current.lastUpdate
        }
        val now = Calendar.getInstance()

        val formatter = if ( profileUpdateDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            profileUpdateDate.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                profileUpdateDate.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH) )
            SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        else
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        when {
            ClashProfileEntity.isFileToken(current.token) -> {
                holder.view.operation = context.getDrawable(R.drawable.ic_edit)
                holder.view.summary = context.getString(R.string.clash_profile_item_summary_file,
                    formatter.format(profileUpdateDate.time))
            }
            ClashProfileEntity.isUrlToken(current.token) -> {
                holder.view.operation = context.getDrawable(R.drawable.ic_sync)
                holder.view.summary = context.getString(R.string.clash_profile_item_summary_url,
                    formatter.format(profileUpdateDate.time))
            }
        }
    }
}