package com.github.kr328.clash

import android.os.Bundle
import com.github.kr328.clash.adapter.ProxyAdapter
import com.github.kr328.clash.callback.IUrlTestCallback
import com.github.kr328.clash.core.event.ErrorEvent
import com.github.kr328.clash.core.model.ProxyPacket
import com.github.kr328.clash.model.ListProxy
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_proxies.*

class ProxyActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proxies)

        setSupportActionBar(activity_proxies_toolbar)

        activity_proxies_list.also {
            it.adapter = ProxyAdapter(this, this::setProxySelected, this::urlTest)
            it.layoutManager = (it.adapter!! as ProxyAdapter).getLayoutManager()
        }

        activity_proxies_swipe.setOnRefreshListener {
            refreshList()
        }

        refreshList()
    }

    private fun setProxySelected(name: String, selected: String) {
        runClash {
            it.setSelectProxy(name, selected)
        }
    }

    private fun urlTest(position: Int, size: Int) {
        val adapter = (activity_proxies_list.adapter as ProxyAdapter)

        runClash {
            val proxies = adapter.elements.subList(position, position+size)
                .filterIsInstance<ListProxy.ListProxyItem>()
                .mapIndexed { index, proxy -> proxy.name to IndexedValue(index, proxy)}
                .toMap()

            for ((_, p) in proxies) {
                p.value.delay = -1
            }

            it.startUrlTest(proxies.keys.toTypedArray(), object: IUrlTestCallback.Stub() {
                override fun onResult(proxy: String?, delay: Long) {
                    if ( proxy == null ) {
                        (adapter.elements[position] as ListProxy.ListProxyHeader).urlTest = false

                        runOnUiThread {
                            adapter.notifyItemRangeChanged(position, size)
                        }

                        return
                    }

                    val p = proxies[proxy] ?: return

                    p.value.delay = delay

                    runOnUiThread {
                        adapter.notifyItemChanged(position + p.index + 1)
                    }
                }
            })
        }
    }

    private fun refreshList() {
        if (!activity_proxies_swipe.isRefreshing)
            activity_proxies_swipe.isRefreshing = true

        runClash { clash ->
            val packet = clash.queryAllProxies()
            val proxies = packet.proxies

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
                .filter {
                    when (packet.mode) {
                        "Global" -> it.value.name == "GLOBAL"
                        "Rule" -> it.value.name != "GLOBAL"
                        else -> false
                    }
                }
                .sortedWith(compareBy(
                    {
                        it.value.type != ProxyPacket.Type.SELECT
                    }, {
                        it.value.order
                    })
                )
                .flatMap {
                    val header =
                        ListProxy.ListProxyHeader(it.value.name, it.value.type, it.value.now)

                    sequenceOf(header) +
                            it.value.all
                                .mapNotNull {
                                    proxies[it]
                                }
                                .map { item ->
                                    ListProxy.ListProxyItem(
                                        item.name,
                                        item.type.toString(),
                                        item.delay, header
                                    )
                                }
                                .asSequence()
                }
                .mapIndexed { index, listProxy ->
                    if (listProxy is ListProxy.ListProxyItem) {
                        if (listProxy.name.hashCode() == listProxy.header.now)
                            listProxy.header.now = index
                    }
                    listProxy
                }
                .toList()

            val listDataOldChanged = (activity_proxies_list.adapter!! as ProxyAdapter)
                .elements
                .filterIsInstance(ListProxy.ListProxyHeader::class.java)
                .map { it.now }

            val listDataChanged = listData
                .filterIsInstance<ListProxy.ListProxyHeader>()
                .map { it.now }

            val changed = if (listDataOldChanged.size != listDataChanged.size)
                (0..listData.size).toList()
            else {
                listDataChanged.mapIndexed { index, i ->
                    if (i == listDataOldChanged[index])
                        emptyList()
                    else
                        listOf(listDataOldChanged[index], i)
                }.flatten()
            }

            runOnUiThread {
                activity_proxies_swipe.isRefreshing = false

                (activity_proxies_list.adapter!! as ProxyAdapter).apply {
                    elements = listData

                    changed.toSet().forEach {
                        notifyItemChanged(it)
                    }
                }
            }
        }
    }

    override fun onErrorEvent(event: ErrorEvent?) {
        Snackbar.make(activity_proxies_root, event?.message ?: "Unknown", Snackbar.LENGTH_LONG).show()
    }
}