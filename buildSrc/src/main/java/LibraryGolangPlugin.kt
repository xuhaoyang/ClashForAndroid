import com.android.build.gradle.LibraryExtension
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.GradleScriptException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class LibraryGolangPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.getByType(LibraryExtension::class.java).apply {
            target.afterEvaluate {
                libraryVariants.forEach { variant ->
                    val abis = defaultConfig.externalNativeBuild.cmake.abiFilters +
                            defaultConfig.externalNativeBuild.ndkBuild.abiFilters

                    val nameCapitalize = variant.name.capitalize(Locale.getDefault())
                    val golangBuildDir = target.golangBuild.resolve(variant.name)

                    val task = target.tasks.register(
                        "externalGolangBuild$nameCapitalize",
                        GolangBuildTask::class.java
                    ) {
                        it.premium.set(variant.flavorName == "premium")
                        it.debug.set(variant.name == "debug")
                        it.nativeAbis.set(abis)
                        it.minSdkVersion.set(defaultConfig.minSdk!!)
                        it.cCompilerBasePath.set(compilerBasePath)
                        it.inputDirectory.set(target.golangSource)
                        it.outputDirectory.set(golangBuildDir)
                    }

                    sourceSets.named(variant.name) {
                        it.jniLibs {
                            srcDir(golangBuildDir)
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

    private val LibraryExtension.compilerBasePath: File
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

            return ndkDirectory.resolve("toolchains/llvm/prebuilt/$host-x86_64/bin")
        }
}