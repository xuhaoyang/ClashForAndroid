package com.github.kr328.clash.service.clash.module

import android.app.Service
import android.content.Intent
import android.content.pm.PackageInfo
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.Clash
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class AppListCacheModule(service: Service) : Module<Unit>(service) {
    private fun PackageInfo.uniqueUidName(): String =
        if (sharedUserId != null && sharedUserId.isNotBlank()) sharedUserId else packageName

    private fun reload() {
        val packages = service.packageManager.getInstalledPackages(0)
            .map { it.applicationInfo.uid to it.uniqueUidName() }

        Clash.notifyInstalledAppsChanged(packages)

        Log.d("Installed ${packages.size} packages cached")
    }

    override suspend fun run() {
        val packageChanged = receiveBroadcast(false, Channel.CONFLATED) {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }

        while (true) {
            reload()

            packageChanged.receive()

            delay(TimeUnit.SECONDS.toMillis(10))
        }
    }
}