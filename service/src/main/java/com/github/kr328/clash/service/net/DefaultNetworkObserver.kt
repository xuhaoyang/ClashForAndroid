// from https://github.com/shadowsocks/shadowsocks-android/blob/master/core/src/main/java/com/github/shadowsocks/net/DefaultNetworkListener.kt
package com.github.kr328.clash.service.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler

class DefaultNetworkObserver(val context: Context, val listener: (Network?) -> Unit) {
    private val handler = Handler()
    private val connectivity = context.getSystemService(ConnectivityManager::class.java)!!
    private var blocking: Boolean = true
    private var current: Network? = null
    private val callback = object: ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if ( blocking )
                return

            current = network

            listener(current)
        }

        override fun onLost(network: Network) {
            if ( blocking )
                return

            if ( current == network )
                current = null

            listener(current)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            if ( blocking )
                return

            if ( network == current )
                listener(current)
        }
    }

    fun register() {
        connectivity.registerNetworkCallback(NetworkRequest.Builder().build(), callback)

        listener(connectivity.activeNetwork)

        handler.postDelayed({
            blocking = false
        }, 1000)
    }

    fun unregister() {
        connectivity.unregisterNetworkCallback(callback)
    }
}