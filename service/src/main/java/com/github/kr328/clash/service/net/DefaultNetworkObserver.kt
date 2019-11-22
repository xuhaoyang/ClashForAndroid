// from https://github.com/shadowsocks/shadowsocks-android/blob/master/core/src/main/java/com/github/shadowsocks/net/DefaultNetworkListener.kt
package com.github.kr328.clash.service.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class DefaultNetworkObserver(val context: Context, val listener: (Network?) -> Unit) {
    private val connectivity = context.getSystemService(ConnectivityManager::class.java)!!
    private val active: Network?
        get() = connectivity.activeNetwork
    private var current: Network? = null
    private val callback = object: ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            current = active

            listener(current)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            if ( current == active )
                listener(active)
        }

        override fun onLost(network: Network) {
            if ( active == current )
                current = null

            listener(current)
        }
    }

    fun register() {
        listener(active)

        connectivity.registerDefaultNetworkCallback(callback)
    }

    fun unregister() {
        connectivity.unregisterNetworkCallback(callback)
    }
}