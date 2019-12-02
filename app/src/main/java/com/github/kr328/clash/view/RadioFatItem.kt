package com.github.kr328.clash.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.github.kr328.clash.R
import com.google.android.material.radiobutton.MaterialRadioButton

class RadioFatItem @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) :
    FrameLayout(context, attributeSet, defStyleAttr, defStyleRes) {
    private val titleView: TextView
    private val summaryView: TextView
    private val operationView: View
    private val radioView: MaterialRadioButton
    private val clickable: View
    private val operationClickable: View

    var isChecked: Boolean
        get() = radioView.isChecked
        set(value) {
            radioView.isChecked = value
        }
    var title: CharSequence?
        get() = titleView.text
        set(value) {
            titleView.text = value
        }
    var summary: CharSequence?
        get() = summaryView.text
        set(value) {
            summaryView.text = value
            if (value?.isNotBlank() != true)
                summaryView.visibility = View.GONE
            else
                summaryView.visibility = View.VISIBLE
        }
    var operation: Drawable?
        get() = operationView.background
        set(value) {
            operationView.background = value
            if (value == null)
                operationClickable.visibility = View.GONE
            else
                operationClickable.visibility = View.VISIBLE
        }

    override fun setOnClickListener(l: OnClickListener?) {
        clickable.setOnClickListener(l)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        clickable.setOnLongClickListener(l)
    }

    fun setOperationOnClickListener(l: OnClickListener?) {
        operationClickable.setOnClickListener(l)
    }

    fun setOperationOnLongClickListener(l: OnLongClickListener?) {
        operationClickable.setOnLongClickListener(l)
    }

    override fun performClick(): Boolean {
        super.performClick()

        return clickable.performClick()
    }

    init {
        with(LayoutInflater.from(context).inflate(R.layout.view_radio_fat_item, this, true)) {
            titleView = findViewById(R.id.view_radio_fat_item_title)
            summaryView = findViewById(R.id.view_radio_fat_item_summary)
            operationView = findViewById(R.id.view_radio_fat_item_operation)
            radioView = findViewById(R.id.view_radio_fat_item_radio)
            clickable = findViewById(R.id.view_radio_fat_item_clickable)
            operationClickable = findViewById(R.id.view_radio_fat_item_operation_clickable)
        }

        context.theme.obtainStyledAttributes(attributeSet, R.styleable.RadioFatItem, 0, 0).apply {
            try {
                title = getString(R.styleable.FatItem_title)
                summary = getString(R.styleable.FatItem_summary)
                operation = getDrawable(R.styleable.FatItem_operation)
            } finally {
                recycle()
            }
        }
    }
}