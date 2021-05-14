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

abstract class GolangBuildTask : DefaultTask() {
    abstract val debug: Property<Boolean>
        @Input get

    abstract val premium: Property<Boolean>
        @Input get

    abstract val nativeAbis: SetProperty<String>
        @Input get

    abstract val minSdkVersion: Property<Int>
        @Input get

    abstract val cCompilerBasePath: DirectoryProperty
        @InputDirectory get

    abstract val inputDirectory: DirectoryProperty
        @InputDirectory get

    abstract val outputDirectory: DirectoryProperty
        @OutputDirectory get

    @TaskAction
    fun build() {
        val src = inputDirectory.get().asFile

        val cmd = if (debug.get()) {
            """
                go build --buildmode=c-shared -trimpath -o "%s" -tags "without_gvisor,without_system,debug${if (premium.get()) ",premium" else ""}"
            """.trimIndent().trim()
        } else {
            """
                go build --buildmode=c-shared -trimpath -o "%s" -tags "without_gvisor,without_system${if (premium.get()) ",premium" else ""}" -ldflags "-s -w"
            """.trimIndent().trim()
        }

        nativeAbis.get().parallelStream().forEach {
            val out = outputDirectory.get().file("$it/libclash.so")

            cmd.format(out).exec(pwd = src, env = generateGolangBuildEnvironment(it))
        }
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
            "CC" to cCompilerBasePath.get().asFile.resolve(compiler).absolutePath,
            "GOOS" to "android",
            "GOARCH" to goArch,
            "GOARM" to goArm,
            "CGO_ENABLED" to "1",
            "CFLAGS" to "-O3 -Werror",
        )
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
}