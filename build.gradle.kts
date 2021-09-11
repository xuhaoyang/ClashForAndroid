@file:Suppress("UNUSED_VARIABLE")

import com.android.build.gradle.BaseExtension
import java.net.URL
import java.util.*

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
        google()
        maven("https://maven.kr328.app/releases")
    }
    dependencies {
        classpath(deps.build.android)
        classpath(deps.build.kotlin.common)
        classpath(deps.build.kotlin.serialization)
        classpath(deps.build.ksp)
        classpath(deps.build.golang)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.kr328.app/releases")
    }
}

subprojects {
    val isApp = name == "app"

    apply(plugin = if (isApp) "com.android.application" else "com.android.library")

    extensions.configure(BaseExtension::class) {
        val minSdkVersion = 21
        val targetSdkVersion = 30
        val buildVersionCode = 204009
        val buildVersionName = "2.4.9"
        val defaultDimension = "feature"

        ndkVersion = "23.0.7599858"

        compileSdkVersion(targetSdkVersion)

        defaultConfig {
            if (isApp) {
                applicationId = "com.github.kr328.clash"
            }

            minSdk = minSdkVersion
            targetSdk = targetSdkVersion

            versionName = buildVersionName
            versionCode = buildVersionCode

            if (!isApp) {
                consumerProguardFiles("consumer-rules.pro")
            }

            resValue("string", "release_name", "v$buildVersionName")
            resValue("integer", "release_code", "$buildVersionCode")

            externalNativeBuild {
                cmake {
                    abiFilters("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
                }
            }
        }

        externalNativeBuild {
            cmake {
                version = "3.18.1"
            }
        }

        if (isApp) {
            packagingOptions {
                excludes.add("DebugProbesKt.bin")
            }
        }

        buildTypes {
            named("release") {
                isMinifyEnabled = isApp
                isShrinkResources = isApp
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }

        productFlavors {
            flavorDimensions(defaultDimension)

            create("foss") {
                dimension = defaultDimension
                versionNameSuffix = ".foss"

                if (isApp) {
                    applicationIdSuffix = ".foss"
                }
            }
            create("premium") {
                dimension = defaultDimension
                versionNameSuffix = ".premium"

                val trackFile = rootProject.file("track.properties")
                if (trackFile.exists()) {
                    val track = Properties().apply {
                        trackFile.inputStream().use(this::load)
                    }

                    buildConfigField("String", "APP_CENTER_KEY", "\"${track.getProperty("appcenter.key")!!}\"")
                } else {
                    buildConfigField("String", "APP_CENTER_KEY", "null")
                }
            }
        }

        buildFeatures.apply {
            dataBinding {
                isEnabled = name != "hideapi"
            }
        }

        if (isApp) {
            splits {
                abi {
                    isEnable = true
                    isUniversalApk = true
                }
            }
        }
    }
}

task("clean", type = Delete::class) {
    delete(rootProject.buildDir)
}

tasks.named<Wrapper>("wrapper") {
    distributionType = Wrapper.DistributionType.ALL

    doLast {
        val sha256 = URL("$distributionUrl.sha256").openStream()
            .use { it.reader().readText().trim() }

        file("gradle/wrapper/gradle-wrapper.properties")
            .appendText("distributionSha256Sum=$sha256")
    }
}