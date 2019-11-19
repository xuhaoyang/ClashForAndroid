package com.github.kr328.clash.core

import android.content.Context
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import org.json.JSONObject
import java.io.*

class Clash(private val context: Context, clashDir: File, private val controllerPath: File) :
    ClashProcess(context, clashDir, controllerPath) {
    companion object {
        const val COMMAND_PING = 0
        const val COMMAND_TUN_START = 1
        const val COMMAND_TUN_STOP = 2
        const val COMMAND_PROFILE_DEFAULT = 3
        const val COMMAND_PROFILE_RELOAD = 4

        const val PING_REPLY = 233
    }

    private val cacheFile = context.cacheDir.resolve("config.yaml")

    fun ping(): Boolean {
        try {
            return runControl(COMMAND_PING) {_, input, _ ->
                input.readInt() == PING_REPLY
            }
        }
        catch (e: IOException) {
            Log.w(TAG, "Clash ping failure", e)
        }
        return false
    }

    @Throws(IOException::class)
    @Synchronized
    fun loadProfile(url: Uri) {
        synchronized(cacheFile) {
            context.contentResolver.openInputStream(url)?.use {
                it.copyTo(FileOutputStream(cacheFile))
            }

            runControl(COMMAND_PROFILE_RELOAD) {_, input, output ->
                output.writeString(JSONObject().apply {
                    put("path", cacheFile.absolutePath)
                }.toString())

                val result = input.readString()
                if (result.isNotEmpty()) {
                    throw IOException(JSONObject(result).getString("error"))
                }
            }
        }
    }

    @Throws(IOException::class)
    fun startTunDevice(fd: FileDescriptor, mtu: Int) {
        runControl(COMMAND_TUN_START) { socket, _, output ->
            socket.setFileDescriptorsForSend(arrayOf(fd))
            socket.outputStream.write(0)
            socket.outputStream.flush()
            output.writeInt(mtu)
            output.writeInt(0x243)
        }
    }

    private fun <R>runControl(command: Int, block: (LocalSocket, DataInputStream, DataOutputStream) -> R): R {
        val socket = LocalSocket()

        socket.connect(LocalSocketAddress(controllerPath.absolutePath, LocalSocketAddress.Namespace.FILESYSTEM))

        val input = DataInputStream(socket.inputStream)
        val output = DataOutputStream(socket.outputStream)

        output.writeInt(command)

        val result = block(socket, input, output)

        try {
            input.close()
            output.close()

            socket.close()
        }
        catch (ignore: IOException) {

        }

        return result
    }

    private fun DataOutputStream.writeString(string: String) {
        this.writeInt(string.length)
        this.writeBytes(string)
    }

    private fun DataInputStream.readString(): String {
        val len = this.readInt()
        val buffer = ByteArray(len)

        this.read(buffer)

        return String(buffer)
    }
}