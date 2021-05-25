package com.github.kr328.clash.tools

import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import golangSource

abstract class ClashCleanTask : Delete() {
    fun applyFrom(project: Project, abis: List<NativeAbi>) {
        val bridge = project.golangSource.resolve("tun2socket")

        delete(bridge.resolve("build"))

        abis.forEach {
            delete(bridge.resolve("build_android_${it.goArch}.go"))
        }
    }
}