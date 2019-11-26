package com.github.kr328.clash.core

import android.net.LocalSocket
import android.net.LocalSocketAddress
import com.github.kr328.clash.core.utils.Log
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File

@Suppress("SameParameterValue")
abstract class BaseClash(private val controllerPath: File) {
    protected fun <R> runControl(
        command: Int,
        block: (LocalSocket, DataInputStream, DataOutputStream) -> R
    ): R {
        val socket = LocalSocket()

        socket.connect(
            LocalSocketAddress(
                controllerPath.absolutePath,
                LocalSocketAddress.Namespace.FILESYSTEM
            )
        )

        val input = DataInputStream(socket.inputStream)
        val output = DataOutputStream(socket.outputStream)

        output.writeInt(command)

        val result = block(socket, input, output)

        runCatching {
            socket.close()
        }

        return result
    }

    protected fun runControl(command: Int) {
        return runControl(command) { _, _, _ -> }
    }

    protected fun <R> runControlNoException(
        command: Int,
        block: (LocalSocket, DataInputStream, DataOutputStream) -> R
    ): R? {
        return try {
            runControl(command, block)
        } catch (e: Exception) {
            Log.w("Run command $command failure", e)
            null
        }
    }

    protected fun runControlNoException(command: Int) {
        try {
            runControl(command)
        } catch (e: Exception) {
            Log.w("Run command $command failure", e)
        }
    }

    protected fun DataOutputStream.writeString(string: String) {
        this.writeInt(string.length)
        this.writeBytes(string)
    }

    protected fun DataInputStream.readString(): String {
        val len = this.readInt()
        val buffer = ByteArray(len)

        this.readFully(buffer)

        return String(buffer)
    }
}