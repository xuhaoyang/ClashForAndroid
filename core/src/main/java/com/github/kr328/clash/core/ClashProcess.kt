package com.github.kr328.clash.core

import android.content.Context
import com.github.kr328.clash.core.event.ProcessEvent
import com.github.kr328.clash.core.utils.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread

class ClashProcess(
    private val context: Context,
    private val clashDir: File,
    private val controllerPath: File,
    private val listener: (ProcessEvent) -> Unit
) {
    companion object {
        private const val CONTROLLER_STATUS_PREFIX = "[CONTROLLER]"

        private val PID_PATTERN = Regex("\\[PID]\\s*(\\d+)")
        private val CONTROLLER_ERROR_PATTERN = Regex("\\[CONTROLLER] ERROR=\\{(.+)\\}")
    }

    private var pid: Int = 0
    private var process: Process? = null

    @Synchronized
    fun start() {
        if (pid > 0)
            return

        try {
            clashDir.mkdirs()
            controllerPath.parentFile?.mkdirs()
            controllerPath.delete()

            extractMMDB()

            val clashPath =
                File(context.applicationInfo.nativeLibraryDir, "libclash.so").absolutePath

            val p = ProcessBuilder().apply {
                command(clashPath, controllerPath.absolutePath)
                directory(clashDir)
            }.start()

            Log.i("Starting clash [$clashPath]")

            val reader = p.inputStream.bufferedReader()
            var line = ""

            // Parse pid
            var currentPid = 0
            while (reader.readLine()?.apply { line = this.trim() } != null) {
                Log.i(line)

                if (PID_PATTERN.matchEntire(line)?.apply {
                        currentPid = groups[1]!!.value.toInt()
                    } != null)
                    break
            }

            while (reader.readLine()?.apply { line = this.trim() } != null) {
                Log.i(line)

                if (line.startsWith(CONTROLLER_STATUS_PREFIX)) {
                    val error = CONTROLLER_ERROR_PATTERN.matchEntire(line)?.groups?.get(1)?.value

                    if (error != null)
                        throw IOException("Controller: $error")

                    break
                }
            }

            process = p
            pid = currentPid

            listener(ProcessEvent.STARTED)

            Log.i("Clash started pid = $pid")

            thread {
                // Redirect stdout to log
                while (reader.readLine()?.apply { line = this } != null) {
                    Log.i(line.trim())
                }

                synchronized(this@ClashProcess) {
                    p.destroy()
                    process = null
                    pid = -1
                }

                listener(ProcessEvent.STOPPED)
            }
        } catch (e: Exception) {
            listener(ProcessEvent.STOPPED)
            throw e
        }
    }

    @Synchronized
    fun getProcessStatus(): ProcessEvent {
        return if (pid > 0)
            ProcessEvent.STARTED
        else
            ProcessEvent.STOPPED
    }

    fun stop() {
        android.os.Process.killProcess(pid)
    }

    private fun extractMMDB() {
        if (context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
            < clashDir.resolve("Country.mmdb").lastModified()
        )
            return

        clashDir.resolve("ui").mkdirs()

        context.resources.assets.open("Country.mmdb").use { input ->
            FileOutputStream(clashDir.resolve("Country.mmdb")).use { output ->
                input.copyTo(output)
            }
        }
    }
}