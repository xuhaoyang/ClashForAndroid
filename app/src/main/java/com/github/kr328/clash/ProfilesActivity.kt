package com.github.kr328.clash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_profiles.*

class ProfilesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_profiles)

        setSupportActionBar(activity_profiles_toolbar)
    }
}