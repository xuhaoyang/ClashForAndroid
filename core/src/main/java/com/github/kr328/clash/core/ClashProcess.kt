package com.github.kr328.clash.core

import android.content.Context
import android.os.Parcelable
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread

abstract class ClashProcess(private val context: Context,
                            private val clashDir: File,
                            private val controllerPath: File,
                            private val listener: (ClashProcessStatus) -> Unit) {
    companion object {
        const val TAG = "ClashForAndroid"

        private const val CLASH_COMMAND = "\"{CLASH}\" \"{CONTROLLER}\" 2>&1"

        private const val CONTROLLER_STATUS_PREFIX = "[CONTROLLER]"

        private val PID_PATTERN = Regex("PID=(\\d+)")
        private val CONTROLLER_ERROR_PATTERN = Regex("\\[CONTROLLER] ERROR=\\{(.+)\\}")
    }

    private var pid: Int = 0
    private var process: Process? = null

    @Synchronized
    fun start() {
        if ( pid > 0 )
            return

        extractMMDB()

        clashDir.mkdirs()
        controllerPath.parentFile?.mkdirs()
        controllerPath.delete()

        val p = ProcessBuilder().apply {
            command("sh")
            directory(clashDir)
        }.start()

        val clashPath = File(context.applicationInfo.nativeLibraryDir,"libclash.so").absolutePath
        val command = CLASH_COMMAND
            .replace("{CLASH}", clashPath)
            .replace("{CONTROLLER}", controllerPath.absolutePath)

        Log.i(TAG, "Starting clash [$command]")

        p.outputStream.use {
            it.write("echo PID=$$\n".toByteArray())
            it.write("exec $command\n".toByteArray())
            it.write("exit\n".toByteArray())
            it.flush()
        }

        var line = ""

        // Parse pid
        var currentPid = 0
        while ( p.inputStream.bufferedReader().readLine()?.apply { line = this.trim() } != null ) {
            if ( PID_PATTERN.matchEntire(line)?.apply { currentPid = groups[1]!!.value.toInt() } != null  )
                break
        }

        while ( p.inputStream.bufferedReader().readLine()?.apply { line = this.trim() } != null ) {
            if ( line.startsWith(CONTROLLER_STATUS_PREFIX) ) {
                val error = CONTROLLER_ERROR_PATTERN.matchEntire(line)?.groups?.get(1)?.value

                if ( error != null )
                    throw IOException("Controller: $error")

                break
            }
        }

        process = p
        pid = currentPid

        listener(ClashProcessStatus(ClashProcessStatus.STATUS_STARTED))

        Log.i(TAG, "Clash started pid = $pid")

        thread {
            // Redirect stdout to log
            while ( p.inputStream.bufferedReader().readLine()?.apply { line = this } != null ) {
                Log.i(TAG, line.trim())
            }

            synchronized(this@ClashProcess) {
                p.destroy()
                process = null
                pid = -1
            }

            listener(ClashProcessStatus(ClashProcessStatus.STATUS_STOPPED))
        }
    }

    @Synchronized
    fun isRunning(): Boolean {
        return pid > 0
    }

    fun stop() {
        android.os.Process.killProcess(pid)
    }

    private fun extractMMDB() {
        if ( context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
            < clashDir.resolve("Country.mmdb").lastModified() )
            return

        context.resources.assets.open("Country.mmdb").use { input ->
            FileOutputStream(clashDir.resolve("Country.mmdb")).use { output ->
                input.copyTo(output)
            }
        }
    }
}