package com.github.kr328.clash.util

import android.os.DeadObjectException
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.remote.Remote
import com.github.kr328.clash.service.remote.IClashManager
import com.github.kr328.clash.service.remote.IProfileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

suspend fun <T> withClash(
    context: CoroutineContext = Dispatchers.IO,
    block: suspend IClashManager.() -> T
): T {
    while (true) {
        val client = Remote.services.clash.get()

        try {
            return withContext(context) { client.block() }
        } catch (e: DeadObjectException) {
            Log.w("Remote services panic")

            Remote.services.clash.reset(client)
        }
    }
}

suspend fun <T> withProfile(
    context: CoroutineContext = Dispatchers.IO,
    block: suspend IProfileManager.() -> T
): T {
    while (true) {
        val client = Remote.services.profile.get()

        try {
            return withContext(context) { client.block() }
        } catch (e: DeadObjectException) {
            Log.w("Remote services panic")

            Remote.services.profile.reset(client)
        }
    }
}
