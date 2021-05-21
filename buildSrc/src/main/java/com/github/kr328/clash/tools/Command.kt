package com.github.kr328.clash.tools

import org.gradle.api.GradleException
import java.io.File
import kotlin.concurrent.thread

class Command(
    private val command: Array<String>,
    workingDir: File,
    environments: Map<String, String>
) {
    private val processBuilder: ProcessBuilder = ProcessBuilder(*command)
        .redirectErrorStream(true)
        .directory(workingDir)
        .apply { environment().putAll(environments) }

    fun exec() {
        val process = processBuilder.start()

        thread {
            process.inputStream.copyTo(System.out)
        }

        val result = process.waitFor()

        if (result != 0) {
            throw GradleException("exec ${command.joinToString(" ")}: exit with $result")
        }
    }

    companion object {
        fun ofGoModuleTidy(workingDir: File): Command {
            return Command(arrayOf("go", "mod", "tidy"), workingDir, System.getenv())
        }

        fun ofGoBuild(
            mode: String,
            output: File,
            tags: List<String>,
            strip: Boolean,
            workingDir: File,
            environments: Map<String, String>
        ): Command {
            val command = mutableListOf("go", "build")

            // go build mode
            command += "-buildmode"
            command += mode

            // output file
            command += "-o"
            command += output.absolutePath

            // trim path prefix
            command += "-trimpath"

            if (tags.isNotEmpty()) {
                command += "-tags"
                command += tags.joinToString(",")
            }

            if (strip) {
                command += "-ldflags"
                command += "-s -w"
            }

            return Command(command.toTypedArray(), workingDir, environments)
        }

        fun ofGoRun(
            file: String,
            args: List<String>,
            workingDir: File,
            environments: Map<String, String>
        ): Command {
            val command = mutableListOf("go", "run")

            command += file
            command += args

            return Command(command.toTypedArray(), workingDir, environments)
        }
    }
}