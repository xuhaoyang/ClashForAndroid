package com.github.kr328.clash.service.clash.module

import android.annotation.TargetApi
import android.app.Service
import android.net.*
import android.os.Build
import androidx.core.content.getSystemService
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.service.util.resolveDns
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class NetworkObserveModule(service: Service) :
    Module<NetworkObserveModule.NetworkChanged>(service) {
    data class NetworkChanged(val network: Network?)

    private val connectivity = service.getSystemService<ConnectivityManager>()!!
    private val networks: Channel<Network?> = Channel(Channel.CONFLATED)
    private val request = NetworkRequest.Builder().apply {
        addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
        if (Build.VERSION.SDK_INT == 23) {  // workarounds for OEM bugs
            removeCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            removeCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)
        }
    }.build()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        private var internet: Boolean = false
        private var network: Network? = null

        override fun onAvailable(network: Network) {
            this.network = network

            networks.trySend(network)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val internet = networkCapabilities
                .hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

            if (this.network == network && this.internet != internet) {
                this.internet = internet

                networks.trySend(network)
            }
        }

        override fun onLost(network: Network) {
            if (this.network == network) {
                networks.trySend(null)
            }
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            if (this.network == network) {
                networks.trySend(network)
            }
        }
    }

    override suspend fun run() {
        try {
            if (Build.VERSION.SDK_INT in 24..27) @TargetApi(24) {
                connectivity.registerDefaultNetworkCallback(callback)
            } else {
                connectivity.requestNetwork(request, callback)
            }
        } catch (e: Exception) {
            Log.w("Observe network changed: $e", e)

            return
        }

        try {
            while (true) {
                val network = networks.receive()

                val dns = connectivity.resolveDns(network)

                Clash.notifyDnsChanged(dns)

                Log.d("Network changed, system dns = $dns")

                enqueueEvent(NetworkChanged(network))
            }
        } finally {
            withContext(NonCancellable) {
                connectivity.unregisterNetworkCallback(callback)

                Clash.notifyDnsChanged(emptyList())
            }
        }
    }
}