// from https://github.com/shadowsocks/shadowsocks-android/blob/master/core/src/main/java/com/github/shadowsocks/net/DefaultNetworkListener.kt
package com.github.kr328.clash.service.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import com.github.kr328.clash.core.utils.Log

class DefaultNetworkObserver(val context: Context, val listener: (Network?) -> Unit) {
    private val handler = Handler()
    private val connectivity = context.getSystemService(ConnectivityManager::class.java)!!
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            handler.removeMessages(0)
            handler.postDelayed({
                listener(rebuildNetworkList())
            }, 500)
        }

        override fun onLost(network: Network) {
            handler.removeMessages(0)
            handler.postDelayed({
                listener(rebuildNetworkList())
            }, 500)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            handler.removeMessages(0)
            handler.postDelayed({
                listener(rebuildNetworkList())
            }, 500)
        }
    }

    fun register() {
        connectivity.registerNetworkCallback(NetworkRequest.Builder().build(), callback)
    }

    fun unregister() {
        connectivity.unregisterNetworkCallback(callback)
    }

    private fun rebuildNetworkList(): Network? {
        return try {
            connectivity.allNetworks
                .flatMap { network ->
                    connectivity.getNetworkCapabilities(network)?.let { listOf(it to network) } ?: emptyList()
                }
                .asSequence()
                .filterNot {
                    it.first.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                            !it.first.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                }
                .sortedBy {
                    when {
                        it.first.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> 0
                        it.first.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> 1
                        it.first.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> 2
                        it.first.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) -> 3
                        it.first.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> 4
                        else -> 5
                    } + if (it.first.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED))
                        -1000
                    else
                        0
                }
                .map {
                    Log.i("Network ${it.first}")
                    it
                }
                .map {
                    it.second
                }
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}