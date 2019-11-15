package com.github.kr328.clash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.kr328.clash.core.Clash

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Clash.loadDefault()
    }
}
