package com.github.kr328.clash.core

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException

abstract class ClashProcess(context: Context, clashDir: File, controllerPath: File) {
    companion object {
        const val TAG = "ClashForAndroid"
        private const val CLASH_COMMAND = "\"{CLASH}\" \"{CONTROLLER}\" 2>&1"
        private val PID_PATTERN = Regex("PID=(\\d+)")
        private const val CONTROLLER_STATUS_PREFIX = "[CONTROLLER]"
        private val CONTROLLER_ERROR_PATTERN = Regex("\\[CONTROLLER] ERROR=\\{(.+)\\}")
    }

    @set:Synchronized @get:Synchronized
    var exited: Boolean = false
    private val pid: Int
    private val process: Process

    init {
        clashDir.mkdirs()
        controllerPath.parentFile?.mkdirs()
        controllerPath.delete()

        process = ProcessBuilder().apply {
            command("sh")
            directory(clashDir)
        }.start()

        val clashPath = File(context.applicationInfo.nativeLibraryDir,"libclash.so").absolutePath
        val command = CLASH_COMMAND
            .replace("{CLASH}", clashPath)
            .replace("{CONTROLLER}", controllerPath.absolutePath)

        Log.i(TAG, "Starting clash [$command]")

        process.outputStream.write("echo PID=$$\n".toByteArray())
        process.outputStream.write("exec $command\n".toByteArray())
        process.outputStream.write("exit\n".toByteArray())
        process.outputStream.flush()

        var line = ""

        // Parse pid
        var currentPid = 0
        while ( process.inputStream.bufferedReader().readLine()?.apply { line = this.trim() } != null ) {
            if ( PID_PATTERN.matchEntire(line)?.apply { currentPid = groups[1]!!.value.toInt() } != null  )
                break
        }
        pid = currentPid

        while ( process.inputStream.bufferedReader().readLine()?.apply { line = this.trim() } != null ) {
            if ( line.startsWith(CONTROLLER_STATUS_PREFIX) ) {
                val error = CONTROLLER_ERROR_PATTERN.matchEntire(line)?.groups?.get(1)?.value

                if ( error != null )
                    throw IOException("Controller: $error")

                break
            }
        }

        Log.i(TAG, "Clash started pid=$pid")
    }

    @Throws(IOException::class)
    fun exec() {
        var line = ""

        // Redirect stdout to log
        while ( process.inputStream.bufferedReader().readLine()?.apply { line = this } != null ) {
            Log.i(TAG, line.trim())
        }

        synchronized(this@ClashProcess) {
            exited = true
        }
    }

    fun stop() {
        android.os.Process.killProcess(pid)
    }
}