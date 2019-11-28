package com.github.kr328.clash

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kr328.clash.adapter.ProxyGroupAdapter
import com.github.kr328.clash.core.model.ProxyPacket
import com.github.kr328.clash.model.ListProxyGroup
import kotlinx.android.synthetic.main.activity_proxies.*

class ProxyActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proxies)

        setSupportActionBar(activity_proxies_toolbar)

        activity_proxies_list.also {
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = ProxyGroupAdapter(this)
            it.visibility = View.GONE
        }

        runClash { clash ->
            val proxies = clash.queryAllProxies().proxies
            val adapterProxies = proxies
                .mapValues {
                    ListProxyGroup.ListProxy(it.value.name,
                        it.value.type.toString(),
                        it.value.delay)
                }

            val listData = proxies
                .asSequence()
                .filter {
                    when (it.value.type) {
                        ProxyPacket.Type.URL_TEST -> true
                        ProxyPacket.Type.FALLBACK -> true
                        ProxyPacket.Type.LOAD_BALANCE -> true
                        ProxyPacket.Type.SELECT -> true
                        else -> false
                    }
                }
                .sortedWith(compareBy(
                    {
                        it.value.type != ProxyPacket.Type.SELECT
                    }, {
                        it.value.name
                    }))
                .map {
                    ListProxyGroup(
                        it.value.name,
                        it.value.type.toString(),
                        it.value.all.mapNotNull { hash ->
                            adapterProxies[hash]
                        },
                        adapterProxies[it.value.now]
                    )
                }
                .toList()

            runOnUiThread {
                (activity_proxies_list.adapter!! as ProxyGroupAdapter).apply {
                    data = listData
                    notifyDataSetChanged()
                }
                activity_proxies_list.visibility = View.VISIBLE
            }
        }
    }
}