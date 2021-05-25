package com.github.kr328.clash.tools

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ClashBuildTask : DefaultTask() {
    abstract val config: Property<BuildConfig>
        @Input get

    abstract val ndkDirectory: DirectoryProperty
        @InputDirectory get

    abstract val inputDirectory: DirectoryProperty
        @InputDirectory get

    abstract val outputDirectory: DirectoryProperty
        @OutputDirectory get

    @TaskAction
    fun build() {
        val input = inputDirectory.file
        val output = outputDirectory.file

        val config = config.get()
        val environment = Environment(ndkDirectory.file, config.minSdkVersion)

        val tags = listOf("without_gvisor", "without_system") +
                (if (config.debug) listOf("debug") else emptyList()) +
                (if (config.premium) listOf("premium") else emptyList())

        Command.ofGoModuleTidy(input).exec()

        config.abis.forEach {
            Command.ofGoRun(
                "make/make.go",
                listOf("tun2socket", ".", "android", it.goArch),
                input.resolve("tun2socket"),
                environment.ofLwipBuild(it)
            ).exec()

            Command.ofGoBuild(
                "c-shared",
                output.resolve("${it.value}/libclash.so"),
                tags,
                !config.debug,
                input,
                environment.ofCoreBuild(it)
            ).exec()
        }
    }

    private val DirectoryProperty.file: File
        get() = get().asFile
}