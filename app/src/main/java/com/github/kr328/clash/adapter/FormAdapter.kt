package com.github.kr328.clash.adapter

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.github.kr328.clash.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FormAdapter(
    private val activity: Activity,
    private val elements: List<Type>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val BASE_REQUEST_CODE = 14654
    }

    interface Type {
        val content: Any?
    }

    data class TextType(
        val icon: Int,
        val title: Int,
        val hint: Int,
        override var content: String = ""
    ) : Type

    data class FilePickerType(
        val icon: Int,
        val title: Int,
        val hint: Int,
        override var content: Uri? = Uri.EMPTY
    ) : Type

    class TextHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.adapter_form_text_icon)
        val title: TextView = view.findViewById(R.id.adapter_form_text_title)
        val text: TextView = view.findViewById(R.id.adapter_form_text_text)
        val clickable: View = view.findViewById(R.id.adapter_form_text_clickable)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Boolean {
        val position = requestCode - BASE_REQUEST_CODE

        if (position in 0..elements.size) {
            val element = elements[position]

            if (element is FilePickerType) {
                if (resultCode == RESULT_OK) {
                    element.content = data.data
                    notifyItemChanged(position)
                }

                return true
            }
        }

        return false
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val current = elements[position]) {
            is TextType -> {
                val castedHolder = holder as TextHolder

                castedHolder.icon.setImageResource(current.icon)
                castedHolder.title.text = activity.getText(current.title)
                castedHolder.text.hint = activity.getText(current.hint)
                castedHolder.text.text = current.content
                castedHolder.clickable.setOnClickListener {
                    showTextEditDialog(
                        castedHolder.text.text.toString(),
                        castedHolder.text.hint.toString()
                    ) {
                        current.content = it
                        notifyItemChanged(position)
                    }
                }
            }
            is FilePickerType -> {
                val castedHolder = holder as TextHolder

                castedHolder.icon.setImageResource(current.icon)
                castedHolder.title.text = activity.getText(current.title)
                castedHolder.text.hint = activity.getText(current.hint)
                castedHolder.text.text = current.content?.pathSegments?.lastOrNull() ?: ""
                castedHolder.clickable.setOnClickListener {
                    activity.startActivityForResult(
                        Intent(Intent.ACTION_GET_CONTENT)
                            .setType("*/*"),
                        BASE_REQUEST_CODE + position
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TextType::class.java.hashCode(), FilePickerType::class.java.hashCode() ->
                TextHolder(
                    LayoutInflater.from(activity).inflate(
                        R.layout.adapter_form_text,
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

    override fun getItemViewType(position: Int): Int {
        return elements[position].javaClass.hashCode()
    }

    private fun showTextEditDialog(initial: String, hint: String, callback: (String) -> Unit) {
        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.clash_profile_name)
            .setView(R.layout.dialog_text_edit)
            .setPositiveButton(R.string.ok) { _, _ -> }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
            .apply {
                setOnShowListener {
                    val data = findViewById<EditText>(R.id.dialog_text_edit).also {
                        it?.hint = hint
                        it?.setText(initial)
                    }
                    getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                        setTextColor(context.getColor(R.color.colorAccent))
                        setOnClickListener {
                            callback(data?.text?.toString() ?: "")
                            dismiss()
                        }
                    }
                    getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                        setTextColor(context.getColor(R.color.colorAccent))
                    }
                }
            }
            .show()
    }
}