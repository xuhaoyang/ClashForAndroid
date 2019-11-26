package com.github.kr328.clash.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.github.kr328.clash.R
import com.github.kr328.clash.fragment.FormFragment
import com.google.android.material.textfield.TextInputEditText

class FormAdapter(
    private val context: Context,
    private val elements: List<FormFragment.FormElement>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class TextHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.adapter_from_text_icon)
        val edit: TextInputEditText = view.findViewById(R.id.adapter_from_text_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            FormFragment.FormType.STRING.ordinal, FormFragment.FormType.FILE.ordinal, FormFragment.FormType.URL.ordinal ->
                TextHolder(
                    LayoutInflater.from(context).inflate(
                        R.layout.adapter_from_text,
                        parent,
                        false
                    )
                )
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemCount(): Int {
        return elements.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val current = elements[position]

        when (holder) {
            is TextHolder -> {
                holder.icon.setImageResource(current.icon)
                holder.edit.hint = context.getString(current.hint)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return elements[position].type.ordinal
    }
}