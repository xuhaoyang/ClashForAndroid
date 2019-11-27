package com.github.kr328.clash

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kr328.clash.adapter.ProxyGroupAdapter
import com.github.kr328.clash.core.model.Proxy

class ProxyActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = RecyclerView(this)

        setContentView(content)

        content.layoutManager = LinearLayoutManager(this)
        content.adapter = ProxyGroupAdapter(this)

        runClash { clash ->
            val proxies = clash.queryAllProxies().proxies

            runOnUiThread {
                (content.adapter!! as ProxyGroupAdapter).data = proxies.toList()
                    .sortedWith(compareBy({ it.second.type == Proxy.TYPE_SELECT }, { it.first }))
            }
        }
    }
}