package com.github.kr328.clash.service.util

import android.net.ConnectivityManager
import android.net.Network

fun ConnectivityManager.resolveDns(network: Network?): List<String> {
    return network?.run(this::getLinkProperties)
        ?.dnsServers
        ?.map { it.asSocketAddressText(53) }
        ?: emptyList()
}