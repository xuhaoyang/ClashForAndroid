package com.github.kr328.clash.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kr328.clash.adapter.FormAdapter
import java.lang.Exception

class FormFragment(private val elements: List<FormElement>) : Fragment() {
    enum class FormType {
        STRING, URL, FILE
    }

    interface ResultDumper {
        class DumpException(message: String) : Exception(message)

        fun dump(element: FormElement, data: Any)
    }
    data class FormElement(val type: FormType, val icon: Int, val title: Int, val hint: Int, val result: ResultDumper)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return RecyclerView(requireActivity()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = FormAdapter(requireActivity(), elements)
        }
    }
}