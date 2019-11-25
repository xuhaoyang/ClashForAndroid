package com.github.kr328.clash.core

import android.content.Context
import android.util.Log
import com.github.kr328.clash.core.Constants.TAG
import com.github.kr328.clash.core.event.*
import com.github.kr328.clash.core.model.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.json.JSONObject
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

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
        const val COMMAND_POLL_EVENT = 6
        const val COMMAND_SET_EVENT_ENABLED = 7

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

    fun queryProxies(): Proxy.Packet {
        return runControl(COMMAND_QUERY_PROXIES) { _, input, _ ->
            val data = input.readString()

            Log.d(TAG, data)

            Json(JsonConfiguration.Stable.copy(strictMode = false))
                .parse(Proxy.Packet.serializer(), data)
        }
    }

    fun pollEvent(listener: (Event) -> Unit) {
        runControl(COMMAND_POLL_EVENT) { _, input, _ ->
            loop@while ( process.getProcessStatus() == ProcessEvent.STARTED
                && !Thread.currentThread().isInterrupted ) {
                when (val type = input.readInt()) {
                    Event.EVENT_CLOSE -> {
                        break@loop
                    }
                    Event.EVENT_LOG -> {
                        listener(Json(JsonConfiguration.Stable).parse(LogEvent.serializer(), input.readString()))
                    }
                    Event.EVENT_PROXY_CHANGED -> {
                        listener(Json(JsonConfiguration.Stable).parse(ProxyChangedEvent.serializer(), input.readString()))
                    }
                    Event.EVENT_TRAFFIC -> {
                        listener(Json(JsonConfiguration.Stable).parse(TrafficEvent.serializer(), input.readString()))
                    }
                    else -> Log.w(TAG, "Invalid event type $type")
                }
            }
        }
    }

    fun setEventEnabled(type: Int, enabled: Boolean) {
        runControl(COMMAND_SET_EVENT_ENABLED) { _, _, output ->
            output.writeInt(type)
            output.writeInt(if ( enabled ) 1 else 0)
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