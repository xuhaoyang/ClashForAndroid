package com.github.kr328.clash

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProxyActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = RecyclerView(this)

        setContentView(content)

        content.layoutManager = LinearLayoutManager(this)

        
    }
}