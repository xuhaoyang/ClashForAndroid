package com.github.kr328.clash

import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.activity_feedback.*

class FeedbackActivity : BaseActivity() {
    @Keep
    class Fragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.feedback, rootKey)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        setSupportActionBar(activity_feedback_toolbar)
    }
}