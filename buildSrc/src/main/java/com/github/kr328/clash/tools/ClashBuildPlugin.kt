package com.github.kr328.clash.tools

import com.android.build.gradle.LibraryExtension
import golangBuild
import golangSource
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.*

class ClashBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.afterEvaluate {
            target.extensions.getByType(LibraryExtension::class.java).apply {
                val abis = defaultConfig.externalNativeBuild.cmake.abiFilters
                    .map { NativeAbi.parse(it) }
                    .distinct()
                val minSdkVersion = defaultConfig.minSdkVersion!!.apiLevel

                target.tasks.register("cleanGolang", ClashCleanTask::class.java) {
                    it.applyFrom(target, abis)

                    target.tasks.getByName("clean").dependsOn(it)
                }

                libraryVariants.forEach { variant ->
                    val config = BuildConfig.of(abis, minSdkVersion, variant)
                    val buildDir = target.golangBuild.resolve(variant.name)
                    val capitalize = variant.name.capitalize(Locale.getDefault())

                    val task = target.tasks.register(
                        "externalGolangBuild$capitalize",
                        ClashBuildTask::class.java
                    ) {
                        it.config.set(config)
                        it.ndkDirectory.set(ndkDirectory)
                        it.inputDirectory.set(target.golangSource)
                        it.outputDirectory.set(buildDir)
                    }

                    sourceSets.named(variant.name) {
                        it.jniLibs {
                            srcDir(buildDir)
                        }
                    }

                    variant.externalNativeBuildProviders.forEach {
                        it.get().dependsOn(task)
                    }
                    target.tasks.filter { it.name.startsWith("buildCMake") }.forEach {
                        it.mustRunAfter(task)
                    }
                }
            }
        }
    }
}