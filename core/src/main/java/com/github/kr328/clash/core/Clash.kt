package com.github.kr328.clash.core

import android.content.Context
import android.util.Log
import com.github.kr328.clash.core.Constants.TAG
import org.json.JSONObject
import java.io.*

class Clash(
    context: Context,
    clashDir: File,
    controllerPath: File,
    listener: (ClashProcessStatus) -> Unit
) : BaseClash(controllerPath) {
    companion object {
        const val COMMAND_PING = 0
        const val COMMAND_TUN_START = 1
        const val COMMAND_TUN_STOP = 2
        const val COMMAND_PROFILE_RELOAD = 4
        const val COMMAND_QUERY_PROXIES = 5

        const val PING_REPLY = 233
    }

    val process = ClashProcess(context, clashDir, controllerPath, listener)

    fun ping(): Boolean {
        try {
            return runControl(COMMAND_PING) { _, input, _ ->
                input.readInt() == PING_REPLY
            }
        } catch (e: IOException) {
            Log.w(TAG, "Clash ping failure", e)
        }
        return false
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
        runControl(COMMAND_TUN_STOP)
    }
}