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
                val properties = Properties().apply {
                    target.rootProject.file("local.properties").inputStream().use(this::load)
                }
                val cmakeDirectory = target.rootProject.file(properties.getProperty("cmake.dir")!!)

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
                        it.ndkDirectory.set(ndkDirectory)
                        it.cmakeDirectory.set(cmakeDirectory)
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
}