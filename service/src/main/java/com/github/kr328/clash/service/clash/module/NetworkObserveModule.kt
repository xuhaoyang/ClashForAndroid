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
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.withContext

class NetworkObserveModule(service: Service) : Module<Network?>(service) {
    private data class Action(val type: Type, val network: Network) {
        enum class Type { Available, Lost, Changed }
    }

    private val connectivity = service.getSystemService<ConnectivityManager>()!!
    private val actions = Channel<Action>(Channel.UNLIMITED)
    private val request = NetworkRequest.Builder().apply {
        addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
        addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
    }.build()
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            actions.trySendBlocking(Action(Action.Type.Available, network))
        }

        override fun onLost(network: Network) {
            actions.trySendBlocking(Action(Action.Type.Lost, network))
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            actions.trySendBlocking(Action(Action.Type.Changed, network))
        }
    }

    override suspend fun run() {
        try {
            connectivity.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            Log.w("Observe network failed: $e", e)

            return
        }

        try {
            var current: Network? = null
            val networks = mutableSetOf<Network>()

            while (true) {
                val action = actions.receive()

                when (action.type) {
                    Action.Type.Available -> {
                        networks.add(action.network)
                    }
                    Action.Type.Lost -> {
                        networks.remove(action.network)
                    }
                    Action.Type.Changed -> {
                        if (current == action.network) {
                            val dns = connectivity.resolveDns(action.network)

                            Clash.notifyDnsChanged(dns)

                            Log.d("Current network changed: ${action.network}: $dns")
                        }

                        continue
                    }
                }

                current = networks.maxByOrNull {
                    connectivity.getNetworkCapabilities(it)?.let { cap ->
                        TRANSPORT_PRIORITY.indexOfFirst { cap.hasTransport(it) }
                    } ?: -1
                }

                val dns = connectivity.resolveDns(current)

                Clash.notifyDnsChanged(dns)

                enqueueEvent(current)

                Log.d("Available network changed: $current of $networks: $dns")
            }
        } finally {
            withContext(NonCancellable) {
                enqueueEvent(null)

                Clash.notifyDnsChanged(emptyList())

                runCatching {
                    connectivity.unregisterNetworkCallback(callback)
                }
            }
        }
    }

    companion object {
        private val TRANSPORT_PRIORITY = sequence {
            yield(NetworkCapabilities.TRANSPORT_CELLULAR)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                yield(NetworkCapabilities.TRANSPORT_LOWPAN)
            }

            yield(NetworkCapabilities.TRANSPORT_BLUETOOTH)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                yield(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            }

            yield(NetworkCapabilities.TRANSPORT_WIFI)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                yield(NetworkCapabilities.TRANSPORT_USB)
            }

            yield(NetworkCapabilities.TRANSPORT_ETHERNET)
        }.toList()
    }
}