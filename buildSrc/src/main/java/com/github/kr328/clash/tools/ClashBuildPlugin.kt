package com.github.kr328.clash.tools

import com.android.build.gradle.LibraryExtension
import golangBuild
import golangSource
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import java.io.File
import java.util.*

class ClashBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.afterEvaluate {
            val cmakeDirectory = resolveCmakeDir(target)

            target.extensions.getByType(LibraryExtension::class.java).apply {
                libraryVariants.forEach { variant ->
                    val config = BuildConfig.of(this, variant)
                    val buildDir = target.golangBuild.resolve(variant.name)
                    val capitalize = variant.name.capitalize(Locale.getDefault())

                    val task = target.tasks.register(
                        "externalGolangBuild$capitalize",
                        ClashBuildTask::class.java
                    ) {
                        it.config.set(config)
                        it.ndkDirectory.set(ndkDirectory)
                        it.cmakeDirectory.set(cmakeDirectory)
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

    private fun resolveCmakeDir(project: Project): File {
        val properties = Properties().apply {
            project.rootProject.file("local.properties").inputStream().use(this::load)
        }

        return project.rootProject.file(
            properties.getProperty("cmake.dir") ?: throw GradleException("cmake.dir not found")
        )
    }
}