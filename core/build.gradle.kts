import com.github.kr328.golang.GolangPlugin
import java.io.FileOutputStream
import java.net.URL
import java.time.Duration

plugins {
    kotlin("android")
    id("com.android.library")
    id("kotlinx-serialization")
    id("golang-android")
}

val geoipDatabaseUrl =
    "https://github.com/Dreamacro/maxmind-geoip/releases/latest/download/Country.mmdb"
val geoipInvalidate = Duration.ofDays(7)!!
val geoipOutput = buildDir.resolve("intermediates/golang_blob")

golang {
    sourceSets {
        create("foss") {
            tags.set(listOf("foss"))
            srcDir.set(file("src/foss/golang"))
        }
        create("premium") {
            tags.set(listOf("premium"))
            srcDir.set(file("src/premium/golang"))
        }
        all {
            fileName.set("libclash.so")
            packageName.set("cfa/native")
        }
    }
}

android {
    productFlavors {
        all {
            externalNativeBuild {
                cmake {
                    arguments("-DGO_SOURCE:STRING=${file("src/main/golang/native")}")
                    arguments("-DGO_OUTPUT:STRING=${GolangPlugin.outputDirOf(project, null, null)}")
                    arguments("-DFLAVOR_NAME:STRING=$name")
                }
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }
}

dependencies {
    implementation(project(":common"))

    implementation(kotlin("stdlib-jdk7"))
    implementation(deps.androidx.core)
    implementation(deps.kotlin.coroutine)
    implementation(deps.kotlin.serialization.json)
}

repositories {
    mavenCentral()
}

task("downloadGeoipDatabase") {
    val databaseFile = geoipOutput.resolve("Country.mmdb")
    val moduleFile = geoipOutput.resolve("go.mod")
    val sourceFile = geoipOutput.resolve("blob.go")

    val moduleContent = """
        module "cfa/blob"
    """.trimIndent()

    val sourceContent = """
        package blob
        
        import _ "embed"
        
        //go:embed Country.mmdb
        var GeoipDatabase []byte
    """.trimIndent()

    outputs.dir(geoipOutput)

    onlyIf {
        System.currentTimeMillis() - databaseFile.lastModified() > geoipInvalidate.toMillis()
    }

    doLast {
        geoipOutput.mkdirs()

        moduleFile.writeText(moduleContent)
        sourceFile.writeText(sourceContent)

        URL(geoipDatabaseUrl).openConnection().getInputStream().use { input ->
            FileOutputStream(databaseFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}

afterEvaluate {
    val downloadTask = tasks["downloadGeoipDatabase"]

    tasks.forEach {
        if (it.name.startsWith("externalGolangBuild")) {
            it.dependsOn(downloadTask)
        }
    }
}