package com.github.kr328.clash.service.clash.module

import android.app.Service
import android.content.Intent
import android.net.*
import android.os.PowerManager
import androidx.core.content.getSystemService
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.service.util.resolveDns
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext

class NetworkObserveModule(service: Service) :
    Module<NetworkObserveModule.NetworkChanged>(service) {
    data class NetworkChanged(val network: Network?)

    private val connectivity = service.getSystemService<ConnectivityManager>()!!
    private val networks: Channel<Network?> = Channel(Channel.CONFLATED)
    private val request = NetworkRequest.Builder().apply {
        addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
        addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
    }.build()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        private var network: Network? = null

        override fun onAvailable(network: Network) {
            if (this.network != network)
                networks.trySend(network)

            this.network = network
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            if (this.network == network)
                networks.trySend(network)
        }
    }

    private fun register(): Boolean {
        return try {
            connectivity.registerNetworkCallback(request, callback)

            true
        } catch (e: Exception) {
            Log.w("Observe network changed: $e", e)

            false
        }
    }

    private fun unregister(): Boolean {
        try {
            connectivity.unregisterNetworkCallback(callback)
        } catch (e: Exception) {
            // ignored
        }

        return false
    }

    override suspend fun run() {
        val screenToggle = receiveBroadcast(false, Channel.CONFLATED) {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        if (service.getSystemService<PowerManager>()?.isInteractive != false) {
            register()
        }

        try {
            while (true) {
                val quit = select<Boolean> {
                    screenToggle.onReceive {
                        when (it.action) {
                            Intent.ACTION_SCREEN_ON ->
                                register()
                            Intent.ACTION_SCREEN_OFF ->
                                unregister()
                            else ->
                                false
                        }
                    }
                    networks.onReceive {
                        val dns = connectivity.resolveDns(it)

                        Clash.notifyDnsChanged(dns)

                        Log.d("Network changed, system dns = $dns")

                        enqueueEvent(NetworkChanged(it))

                        false
                    }
                }
                if (quit) {
                    return
                }
            }
        } finally {
            withContext(NonCancellable) {
                unregister()

                Clash.notifyDnsChanged(emptyList())
            }
        }
    }
}