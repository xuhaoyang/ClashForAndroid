import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.GradleException
import org.gradle.api.tasks.Exec
import java.io.File
import java.io.FileReader

import java.util.Properties

open class GolangBuildTask : Exec() {
    private lateinit var options: GolangBuildOptions
    private lateinit var abi: String

    override fun exec() {
        val toolchainRoot = findAndroidNdkPath().resolve("toolchains/llvm/prebuilt/${detectOsType()}/bin")
        val linkerPrefix = toolChainPrefix()
        val compilerPrefix = linkerPrefix + options.platform

        if ( !toolchainRoot.exists() )
            throw GradleException("Compiler not found")

        workingDir = options.sourceDir

        environment.put("GOARCH", golangArch())
        environment.put("GOOS", "android")
        environment.put("CGO_ENABLED", "1")
        environment.put("GOPATH", project.buildDir.resolve("intermediates/gopath").absolutePath)
        environment.put("CXX", toolchainRoot.resolve(compilerPrefix + "-clang++".cmd()).absolutePath)
        environment.put("CC", toolchainRoot.resolve(compilerPrefix + "-clang".cmd()).absolutePath)
        environment.put("LD", toolchainRoot.resolve(linkerPrefix + "ld".exe()).absolutePath)

        commandLine = listOf("go".exe(), "build", "-o", options.outputDir.resolve("$abi/libclash.so").absolutePath)

        super.exec()
    }

    fun setOptions(options: GolangBuildOptions, abi: String) {
        this.options = options
        this.abi = abi
    }

    private fun findAndroidNdkPath(): File {
        val properties = FileReader(project.rootProject.file("local.properties")).use {
            Properties().apply { load(it) }
        }

        return properties.getProperty("ndk.dir")?.let { File(it) }?.takeIf { it.exists() }
            ?: throw GradleException("Android NDK not found.")
    }

    private fun detectOsType(): String {
        return when {
            Os.isFamily(Os.FAMILY_WINDOWS) -> "windows-x86_64"
            Os.isFamily(Os.FAMILY_MAC) -> "darwin-x86_64"
            Os.isFamily(Os.FAMILY_UNIX) -> "linux-x86_64"
            else -> throw GradleException("Unsupported Build OS ${System.getenv("os.name")}")
        }
    }

    private fun golangArch(): String {
        return when (abi) {
            "armeabi-v7a" -> "arm"
            "arm64-v8a" -> "arm64"
            "x86" -> "i686"
            "x86_64" -> "amd64"
            else -> throw GradleException("Unsupported arch $abi")
        }
    }

    private fun toolChainPrefix(): String {
        return when (abi) {
            "armeabi-v7a" -> "arm-linux-androideabi"
            "arm64-v8a" -> "aarch64-linux-android"
            "x86" -> "i686-linux-android"
            "x86_64" -> "x86_64-linux-android"
            else -> throw GradleException("Unsupported arch $abi")
        }
    }

    private fun String.exe(): String {
        return if ( Os.isFamily(Os.FAMILY_WINDOWS) )
            "$this.exe"
        else
            this
    }

    private fun String.cmd(): String {
        return if ( Os.isFamily(Os.FAMILY_WINDOWS) )
            "$this.cmd"
        else
            this
    }
}
