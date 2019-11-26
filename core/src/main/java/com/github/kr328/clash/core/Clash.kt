package com.github.kr328.clash.core

import android.content.Context
import android.net.LocalSocket
import android.util.Log
import com.github.kr328.clash.core.Constants.TAG
import com.github.kr328.clash.core.event.LogEvent
import com.github.kr328.clash.core.event.ProcessEvent
import com.github.kr328.clash.core.event.ProxyChangedEvent
import com.github.kr328.clash.core.event.TrafficEvent
import com.github.kr328.clash.core.model.Proxy
import com.github.kr328.clash.core.model.ProxyPacket
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.json.JSONObject
import java.io.File
import java.io.FileDescriptor
import java.io.IOException

class Clash(
    context: Context,
    clashDir: File,
    controllerPath: File,
    listener: (ProcessEvent) -> Unit
) : BaseClash(controllerPath) {
    companion object {
        const val COMMAND_PING = 0
        const val COMMAND_TUN_START = 1
        const val COMMAND_TUN_STOP = 2
        const val COMMAND_PROFILE_RELOAD = 4
        const val COMMAND_QUERY_PROXIES = 5
        const val COMMAND_PULL_TRAFFIC = 6
        const val COMMAND_PULL_LOG = 7
        const val COMMAND_PULL_PROXY_CHANGED = 8

        const val PING_REPLY = 233
    }

    val process = ClashProcess(context, clashDir, controllerPath, listener)

    fun ping(): Boolean {
        return runControlNoException(COMMAND_PING) { _, input, _ ->
            input.readInt() == PING_REPLY
        } ?: false
    }

    fun loadProfile(file: File) {
        runControl(COMMAND_PROFILE_RELOAD) { _, input, output ->
            output.writeString(JSONObject().apply {
                put("path", file.absolutePath)
            }.toString())

            val result = input.readString()
            if (result.isNotEmpty()) {
                throw IOException(JSONObject(result).getString("error"))
            }
        }
    }

    fun queryProxies(): ProxyPacket {
        return runControl(COMMAND_QUERY_PROXIES) { _, input, _ ->
            val data = input.readString()

            Log.d(TAG, data)

            Json(JsonConfiguration.Stable.copy(strictMode = false))
                .parse(ProxyPacket.serializer(), data)
        }
    }

    fun pullTrafficEvent(initial: (LocalSocket) -> Unit, callback: (TrafficEvent) -> Unit) {
        runControlNoException(COMMAND_PULL_TRAFFIC) { socket, input, _ ->
            initial(socket)

            while (!Thread.currentThread().isInterrupted) {
                callback(
                    Json(JsonConfiguration.Stable).parse(
                        TrafficEvent.serializer(),
                        input.readString()
                    )
                )
            }
        }
    }

    fun pullLogsEvent(initial: (LocalSocket) -> Unit, callback: (LogEvent) -> Unit) {
        runControlNoException(COMMAND_PULL_LOG) { socket, input, _ ->
            initial(socket)

            while (!Thread.currentThread().isInterrupted) {
                callback(
                    Json(JsonConfiguration.Stable).parse(
                        LogEvent.serializer(),
                        input.readString()
                    )
                )
            }
        }
    }

    fun pullProxyChangedEvent(
        initial: (LocalSocket) -> Unit,
        callback: (ProxyChangedEvent) -> Unit
    ) {
        runControlNoException(COMMAND_PULL_PROXY_CHANGED) { socket, input, _ ->
            initial(socket)

            while (!Thread.currentThread().isInterrupted) {
                callback(
                    Json(JsonConfiguration.Stable).parse(
                        ProxyChangedEvent.serializer(),
                        input.readString()
                    )
                )
            }
        }
    }

    fun startTunDevice(fd: FileDescriptor, mtu: Int) {
        runControl(COMMAND_TUN_START) { socket, _, output ->
            socket.setFileDescriptorsForSend(arrayOf(fd))
            socket.outputStream.write(0)
            socket.outputStream.flush()
            output.writeInt(mtu)
            output.writeInt(0x243)
        }
    }

    fun stopTunDevice() {
        runControlNoException(COMMAND_TUN_STOP)
    }
}