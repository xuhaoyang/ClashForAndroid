import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.GradleScriptException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

abstract class GolangBuildTask : DefaultTask() {
    abstract val debug: Property<Boolean>
        @Input get

    abstract val premium: Property<Boolean>
        @Input get

    abstract val nativeAbis: SetProperty<String>
        @Input get

    abstract val minSdkVersion: Property<Int>
        @Input get

    abstract val ndkDirectory: DirectoryProperty
        @InputDirectory get

    abstract val cmakeDirectory: DirectoryProperty
        @InputDirectory get

    abstract val inputDirectory: DirectoryProperty
        @InputDirectory get

    abstract val outputDirectory: DirectoryProperty
        @OutputDirectory get

    @TaskAction
    fun build() {
        val src = inputDirectory.get().asFile

        val generateCmd = """go run make/make.go bridge native build android %s"""

        val buildCmd = if (debug.get()) {
            """go build --buildmode=c-shared -trimpath -o "%s" -tags "without_gvisor,without_system,debug${if (premium.get()) ",premium" else ""}"""
        } else {
            """go build --buildmode=c-shared -trimpath -o "%s" -tags "without_gvisor,without_system${if (premium.get()) ",premium" else ""}" -ldflags "-s -w""""
        }

        "go mod tidy".exec(pwd = src)

        nativeAbis.get().parallelStream().forEach {
            val out = outputDirectory.get().file("$it/libclash.so")

            generateCmd.format(it.toGoArch()).exec(pwd = src.resolve("tun2socket/bridge"), env = generateGolangGenerateEnvironment(it))
            buildCmd.format(out).exec(pwd = src, env = generateGolangBuildEnvironment(it))
        }
    }

    private fun generateGolangGenerateEnvironment(abi: String): Map<String, String> {
        val path = cmakeDirectory.get().asFile.absolutePath + File.pathSeparator + System.getenv("PATH")

        return mapOf(
            "PATH" to path,
            "CMAKE_SYSTEM_NAME" to "Android",
            "CMAKE_ANDROID_NDK" to ndkDirectory.get().asFile.absolutePath,
            "CMAKE_ANDROID_ARCH_ABI" to abi,
            "CMAKE_SYSTEM_VERSION" to minSdkVersion.get().toString()
        )
    }

    private fun generateGolangBuildEnvironment(abi: String): Map<String, String> {
        val (goArch, goArm) = when (abi) {
            "arm64-v8a" -> "arm64" to ""
            "armeabi-v7a" -> "arm" to "7"
            "x86" -> "386" to ""
            "x86_64" -> "amd64" to ""
            else -> throw UnsupportedOperationException("unsupported abi: $abi")
        }

        val compiler = when (abi) {
            "armeabi-v7a" ->
                "armv7a-linux-androideabi${minSdkVersion.get()}-clang"
            "arm64-v8a" ->
                "aarch64-linux-android${minSdkVersion.get()}-clang"
            "x86" ->
                "i686-linux-android${minSdkVersion.get()}-clang"
            "x86_64" ->
                "x86_64-linux-android${minSdkVersion.get()}-clang"
            else ->
                throw GradleScriptException(
                    "Unsupported abi $abi",
                    FileNotFoundException("Unsupported abi $abi")
                )
        }

        return mapOf(
            "CC" to compilerBasePath.resolve(compiler).absolutePath,
            "GOOS" to "android",
            "GOARCH" to goArch,
            "GOARM" to goArm,
            "CGO_ENABLED" to "1",
            "CFLAGS" to "-O3 -Werror",
            "CMAKE_ARGS" to "-DCMAKE_TOOLCHAIN_FILE=${ndkDirectory.get().asFile.absolutePath}/build/cmake/android.toolchain.cmake -DANDROID_ABI=$abi -DANDROID_PLATFORM=android-${minSdkVersion.get()} -DCMAKE_BUILD_TYPE=Release",
            "PATH" to cmakeDirectory.get().asFile.absolutePath + File.pathSeparator + System.getenv("PATH")
        )
    }

    private fun String.toGoArch(): String {
        return when (this) {
            "arm64-v8a" -> "arm64"
            "armeabi-v7a" -> "arm"
            "x86" -> "386"
            "x86_64" -> "amd64"
            else -> throw UnsupportedOperationException("unsupported abi: $this")
        }
    }

    private fun String.exec(
        pwd: File,
        env: Map<String, String> = System.getenv()
    ): String {
        val process = ProcessBuilder().run {
            if (Os.isFamily(Os.FAMILY_WINDOWS))
                command("cmd.exe", "/c", this@exec)
            else
                command("bash", "-c", this@exec)

            environment().putAll(env)
            directory(pwd)

            redirectErrorStream(true)

            start()
        }

        val outputStream = ByteArrayOutputStream()
        process.inputStream.copyTo(outputStream)

        if (process.waitFor() != 0) {
            println(outputStream.toString("utf-8"))
            throw GradleScriptException("Exec $this failure", IOException())
        }

        return outputStream.toString("utf-8")
    }

    private val compilerBasePath: File
        get() {
            val host = when {
                Os.isFamily(Os.FAMILY_WINDOWS) ->
                    "windows"
                Os.isFamily(Os.FAMILY_MAC) ->
                    "darwin"
                Os.isFamily(Os.FAMILY_UNIX) ->
                    "linux"
                else ->
                    throw GradleScriptException(
                        "Unsupported host",
                        FileNotFoundException("Unsupported host")
                    )
            }

            return ndkDirectory.get().asFile.resolve("toolchains/llvm/prebuilt/$host-x86_64/bin")
        }
}