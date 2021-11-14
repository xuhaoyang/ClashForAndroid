package com.github.kr328.clash.service.clash.module

import android.app.Service
import android.net.*
import android.os.Build
import androidx.core.content.getSystemService
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.service.util.resolveDns
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext

class NetworkObserveModule(service: Service) :
    Module<NetworkObserveModule.NetworkChanged>(service) {
    data class NetworkChanged(val network: Network?)

    private val connectivity = service.getSystemService<ConnectivityManager>()!!
    private val networks: Channel<Network?> = Channel(Channel.CONFLATED)
    private val request = NetworkRequest.Builder().apply {
        addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        addTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH)
        addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
        addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
        addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
        if (Build.VERSION.SDK_INT >= 26)
            addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
        if (Build.VERSION.SDK_INT >= 27)
            addTransportType(NetworkCapabilities.TRANSPORT_LOWPAN)
        if (Build.VERSION.SDK_INT >= 31)
            addTransportType(NetworkCapabilities.TRANSPORT_USB)
        if (Build.VERSION.SDK_INT == 23) {  // workarounds for OEM bugs
            removeCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            removeCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)
        }
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

    override suspend fun run() {
        try {
            connectivity.registerNetworkCallback(request, callback)
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