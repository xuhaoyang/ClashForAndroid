package com.github.kr328.clash

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kr328.clash.adapter.FormAdapter
import kotlinx.android.synthetic.main.activity_import_url.*

class ImportUrlActivity : BaseActivity() {
    private val elements = listOf<FormAdapter.Type>(
        FormAdapter.TextType(
            R.drawable.ic_about,
            R.string.clash_profile_name,
            R.string.clash_profile_name_hint),
        FormAdapter.TextType(
            R.drawable.ic_link,
            R.string.clash_profile_url,
            R.string.clash_profile_url_hint
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_url)

        setSupportActionBar(activity_import_url_toolbar)

        activity_import_url_form.also {
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = FormAdapter(this, elements)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null && (activity_import_url_form.adapter as FormAdapter)
                .onActivityResult(requestCode, resultCode, data))
            return

        super.onActivityResult(requestCode, resultCode, data)
    }
}